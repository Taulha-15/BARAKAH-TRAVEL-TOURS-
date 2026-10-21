
using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace BarakahTravel.Web.Pages;

[Authorize(Policy = "CustomerOnly")]
public class MyApplicationsModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public MyApplicationsModel(
        IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    public List<ApplicationDetails> Applications { get; set; } = new();

    public string? ErrorMessage { get; set; }

    public async Task OnGetAsync()
    {
        if (!Request.Cookies.TryGetValue(
            "BarakahTravel.Web.Customer",
            out var customerCookie))
        {
            ErrorMessage = "Please log in again.";
            return;
        }

        try
        {
            var client = _httpClientFactory
                .CreateClient("BarakahAPI");

            using var request = new HttpRequestMessage(
                HttpMethod.Get,
                "api/Customers/my-applications");

            // Pass the signed-in customer's authentication cookie
            // to the protected API endpoint.
            request.Headers.TryAddWithoutValidation(
                "Cookie",
                $"BarakahTravel.Web.Customer={customerCookie}");

            using var response = await client.SendAsync(request);

            if (response.StatusCode == HttpStatusCode.Unauthorized ||
                response.StatusCode == HttpStatusCode.Forbidden)
            {
                ErrorMessage =
                    "Your session could not be verified. Please log in again.";
                return;
            }

            if (!response.IsSuccessStatusCode)
            {
                ErrorMessage =
                    "Unable to load your Umrah applications.";
                return;
            }

            Applications = await response.Content
                .ReadFromJsonAsync<List<ApplicationDetails>>()
                ?? new List<ApplicationDetails>();
        }
        catch (HttpRequestException)
        {
            ErrorMessage =
                "Unable to connect to the server. Please try again.";
        }
        catch (TaskCanceledException)
        {
            ErrorMessage =
                "The request timed out. Please try again.";
        }
    }


    public class ApplicationDetails
    {
        public int ApplicationId { get; set; }

        public int PackageId { get; set; }

        public string PackageName { get; set; } = "";

        public string ApplicationStatus { get; set; } = "";

        public DateTime? SubmittedAt { get; set; }

        public DateTime? ReviewedAt { get; set; }

        public DateTime CreatedAt { get; set; }
    }

}
