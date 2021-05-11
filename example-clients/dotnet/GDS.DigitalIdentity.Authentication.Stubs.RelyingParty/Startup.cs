
using System.IdentityModel.Tokens.Jwt;

using System.Threading.Tasks;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.IdentityModel.Tokens;
using Microsoft.IdentityModel.Logging;
using System;

namespace GDS.DigitalIdentity.Authentication.Stubs.RelyingParty
{
    public class Startup
    {
        public Startup(IConfiguration configuration)
        {
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            services.AddControllersWithViews();

            JwtSecurityTokenHandler.DefaultMapInboundClaims = false;
            
            services.AddAuthentication(options =>
            {
                options.DefaultScheme = "cookie";
                options.DefaultChallengeScheme = "oidc";
            })
                .AddCookie("cookie", options =>
                {
                    options.Cookie.Name = "dummy-service-cookie";
                    options.Cookie.IsEssential = true;
                    options.ExpireTimeSpan = System.TimeSpan.FromHours(1);
                    options.Events.OnSigningOut = async e =>
                    {
                        try
                        {
                            await e.HttpContext.RevokeUserRefreshTokenAsync();
                        }
                        catch(Exception ex)
                        {
                            LogHelper.LogExceptionMessage(ex);
                        }
                    };
                })
                .AddOpenIdConnect("oidc", options =>
                {
                    options.Authority = "http://localhost:8080";
                    options.RequireHttpsMetadata = false;
                    options.UsePkce = false;
                    
                    options.ClientId = "some_client_id";
                    options.ClientSecret = "password";

                    options.ResponseType = "code";
                    options.ResponseMode = "query";
                    options.ProtocolValidator.RequireNonce = false;
                    options.ProtocolValidator.RequireSub = false;

                    options.Scope.Clear();
                    options.Scope.Add("openid");
                    options.Scope.Add("profile");
                    options.Scope.Add("email");

                    options.GetClaimsFromUserInfoEndpoint = true;
                    options.SaveTokens = true;

                    options.TokenValidationParameters = new TokenValidationParameters
                    {
                        NameClaimType = "name",
                        RoleClaimType = "role"
                    };
                    options.Events.OnAuthorizationCodeReceived = e =>
                    {
                        LogHelper.LogInformation("Received Authorisation Code", e.ProtocolMessage.Code);
                        return Task.CompletedTask;
                    };
                    options.Events.OnRedirectToIdentityProvider = e =>
                    {
                        LogHelper.LogInformation("Redirecting to auth provider", e.ProtocolMessage.RedirectUri);
                        return Task.CompletedTask;
                    };
                    options.Events.OnTokenResponseReceived = e =>
                    {
                        LogHelper.LogInformation("Access Token Received", e.ProtocolMessage.AccessToken);
                        LogHelper.LogInformation("ID Token Received", e.ProtocolMessage.IdToken);
                        return Task.CompletedTask;
                    };
                    options.Events.OnUserInformationReceived = e =>
                    {
                        LogHelper.LogInformation("User Info Received");
                        return Task.CompletedTask;
                    };
                    options.Events.OnRedirectToIdentityProviderForSignOut = e =>
                    {
                        e.ProtocolMessage.IssuerAddress = "http://localhost:8080/logout";
                        return Task.CompletedTask;
                    };
                });
            services.AddAccessTokenManagement();
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IWebHostEnvironment env)
        {
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
                IdentityModelEventSource.ShowPII = true;
            }
            else
            {
                app.UseExceptionHandler("/Home/Error");
                // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
                app.UseHsts();
            }
            app.UseHttpsRedirection();
            app.UseStaticFiles();

            app.UseRouting();

            app.UseAuthentication();
            app.UseAuthorization();

            app.UseEndpoints(endpoints =>
            {
                endpoints.MapDefaultControllerRoute()
                    .RequireAuthorization();
            });
        }
    }
}
