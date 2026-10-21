
using BarakahTravel.Web.Models;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;
using System.Net.Http.Json;

namespace BarakahTravel.Web.Pages;

[Authorize(Policy = "CustomerOnly")]
public class ApplyModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public ApplyModel(IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    public UmrahPackage? SelectedPackage { get; set; }

    public string? ErrorMessage { get; set; }

    public async Task<IActionResult> OnGetAsync(int packageId)
    {
        if (packageId <= 0)
        {
            return BadRequest("Invalid package selection.");
        }

        try
        {
            var client = _httpClientFactory
                .CreateClient("BarakahAPI");

            var packages = await client
                .GetFromJsonAsync<List<UmrahPackage>>("api/Packages");

            SelectedPackage = packages?
                .FirstOrDefault(p => p.PackageId == packageId);

            if (SelectedPackage == null)
            {
                return NotFound();
            }
        }
        catch (HttpRequestException)
        {
            ErrorMessage =
                "Unable to load the selected package. Please try again.";
        }
        catch (TaskCanceledException)
        {
            ErrorMessage =
                "The request timed out. Please try again.";
        }
        catch (System.Text.Json.JsonException)
        {
            ErrorMessage =
                "Package information could not be loaded.";
        }

        return Page();
    }

    public async Task<IActionResult> OnPostAsync(int packageId)
    {
        if (packageId <= 0)
        {
            return BadRequest("Invalid package selection.");
        }

        var client = _httpClientFactory.CreateClient("BarakahAPI");

        var authCookie = Request.Cookies["BarakahTravel.Web.Customer"];

        if (string.IsNullOrEmpty(authCookie))
        {
            return Challenge();
        }

        using var request = new HttpRequestMessage(
            HttpMethod.Post,
            "api/Customers/my-applications");

        request.Headers.Add(
            "Cookie",
            $"BarakahTravel.Web.Customer={authCookie}");

        request.Headers.Add("X-Barakah-Portal", "Apply");

        request.Content = JsonContent.Create(new
        {
            PackageId = packageId
        });

        try
        {
            using var response = await client.SendAsync(request);

            if (response.IsSuccessStatusCode)
            {
                return RedirectToPage("/MyApplications");
            }

            if (response.StatusCode == System.Net.HttpStatusCode.Conflict)
            {
                ModelState.AddModelError(
                    string.Empty,
                    "You already have an active application for this package.");
            }
            else
            {
                ModelState.AddModelError(
                    string.Empty,
                    "Unable to submit your application. Please try again.");
            }
        }
        catch (HttpRequestException)
        {
            ModelState.AddModelError(
                string.Empty,
                "Unable to connect to the application service.");
        }
        catch (TaskCanceledException)
        {
            ModelState.AddModelError(
                string.Empty,
                "The application request timed out. Please try again.");
        }

        // Reload package details if submission fails.
        await OnGetAsync(packageId);

        return Page();
    }

}
