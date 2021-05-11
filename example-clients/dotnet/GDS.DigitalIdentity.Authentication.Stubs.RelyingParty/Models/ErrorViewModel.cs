using System;

namespace GDS.DigitalIdentity.Authentication.Stubs.RelyingParty.Models
{
    public class ErrorViewModel
    {
        public string RequestId { get; set; }

        public bool ShowRequestId => !string.IsNullOrEmpty(RequestId);
    }
}
