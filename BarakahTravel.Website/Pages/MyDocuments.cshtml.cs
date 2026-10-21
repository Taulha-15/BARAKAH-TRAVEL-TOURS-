
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net.Http.Headers;
using System.Net.Http.Json;

namespace BarakahTravel.Web.Pages;

[Authorize(Policy = "CustomerOnly")]
public class MyDocumentsModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public MyDocumentsModel(
        IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    public List<CustomerApplication> Applications { get; set; }
        = new();

    public string? ErrorMessage { get; set; }

    public string? SuccessMessage { get; set; }

    [BindProperty]
    public int ApplicationId { get; set; }

    [BindProperty]
    public string DocumentType { get; set; } = string.Empty;

    [BindProperty]
    public IFormFile? File { get; set; }

    public async Task OnGetAsync()
    {
        try
        {
            var client = _httpClientFactory
                .CreateClient("BarakahAPI");

            using var request = new HttpRequestMessage(
                HttpMethod.Get,
                "api/Customers/my-applications");

            var authCookie =
                Request.Cookies["BarakahTravel.Web.Customer"];

            if (string.IsNullOrEmpty(authCookie))
            {
                ErrorMessage = "Please log in again.";
                return;
            }

            request.Headers.Add(
                "Cookie",
                $"BarakahTravel.Web.Customer={authCookie}");

            using var response = await client.SendAsync(request);

            if (!response.IsSuccessStatusCode)
            {
                ErrorMessage =
                    "Unable to load your Umrah applications.";
                return;
            }

            Applications = await response.Content
                .ReadFromJsonAsync<List<CustomerApplication>>()
                ?? new();
        }
        catch (HttpRequestException)
        {
            ErrorMessage =
                "Unable to connect to the application service.";
        }
        catch (TaskCanceledException)
        {
            ErrorMessage =
                "The request timed out. Please try again.";
        }
    }

    public async Task<IActionResult> OnPostAsync()
    {
        if (ApplicationId <= 0 ||
            string.IsNullOrWhiteSpace(DocumentType) ||
            File == null ||
            File.Length == 0)
        {
            ErrorMessage = "Please complete all document fields.";
            await OnGetAsync();
            return Page();
        }

        const long maxFileSize = 5 * 1024 * 1024;

        var allowedTypes = new[]
        {
            "Passport Copy",
            "ID Document",
            "Other"
        };

        var allowedExtensions = new[]
        {
            ".pdf", ".jpg", ".jpeg", ".png"
        };

        var extension = Path.GetExtension(File.FileName)
            .ToLowerInvariant();

        if (File.Length > maxFileSize ||
            !allowedExtensions.Contains(extension) ||
            !allowedTypes.Contains(DocumentType))
        {
            ErrorMessage =
                "Invalid document type or file. " +
                "Use PDF, JPG or PNG up to 5 MB.";

            await OnGetAsync();
            return Page();
        }

        try
        {
            var client = _httpClientFactory
                .CreateClient("BarakahAPI");

            var authCookie =
                Request.Cookies["BarakahTravel.Web.Customer"];

            if (string.IsNullOrEmpty(authCookie))
            {
                return Challenge();
            }

            using var form = new MultipartFormDataContent();

            form.Add(
                new StringContent(ApplicationId.ToString()),
                "ApplicationId");

            form.Add(
                new StringContent(DocumentType),
                "DocumentType");

            await using var stream = File.OpenReadStream();

            using var fileContent = new StreamContent(stream);

            fileContent.Headers.ContentType =
                new MediaTypeHeaderValue(
                    File.ContentType == "application/pdf"
                        ? "application/pdf"
                        : "application/octet-stream");

            form.Add(
                fileContent,
                "File",
                Path.GetFileName(File.FileName));

            using var request = new HttpRequestMessage(
                HttpMethod.Post,
                "api/Documents/upload");

            // Forward the logged-in customer's cookie.
            request.Headers.Add(
                "Cookie",
                $"BarakahTravel.Web.Customer={authCookie}");

            // Required by the Documents API upload endpoint.
            request.Headers.Add(
                "X-Barakah-Portal",
                "MyDocuments");

            request.Content = form;

            using var response = await client.SendAsync(request);

            if (response.IsSuccessStatusCode)
            {
                SuccessMessage =
                    "Document uploaded successfully.";

                ApplicationId = 0;
                DocumentType = string.Empty;
                File = null;
            }
            else
            {
                var apiError = await response.Content
                    .ReadAsStringAsync();

                ErrorMessage =
                    $"Upload failed (HTTP {(int)response.StatusCode}): " +
                    apiError;
            }
        }
        catch (HttpRequestException)
        {
            ErrorMessage =
                "Unable to connect to the document service.";
        }
        catch (TaskCanceledException)
        {
            ErrorMessage =
                "The upload timed out. Please try again.";
        }

        await OnGetAsync();

        return Page();
    }

    public class CustomerApplication
    {
        public int ApplicationId { get; set; }

        public string PackageName { get; set; }
            = string.Empty;

        public string ApplicationStatus { get; set; }
            = string.Empty;
    }
}
