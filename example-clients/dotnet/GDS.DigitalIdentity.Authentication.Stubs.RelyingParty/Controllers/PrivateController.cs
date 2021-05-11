using Microsoft.AspNetCore.Mvc;

namespace GDS.DigitalIdentity.Authentication.Stubs.RelyingParty.Controllers
{
    public class PrivateController : Controller
    {
        // GET: /<controller>/
        public IActionResult Index()
        {
            return View();
        }
    }
}
