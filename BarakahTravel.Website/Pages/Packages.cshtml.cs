
using BarakahTravel.Web.Models;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace BarakahTravel.Web.Pages;

public class PackagesModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public PackagesModel(IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    public List<UmrahPackage> Packages { get; set; } = new();

    public string? ErrorMessage { get; set; }

    public async Task OnGetAsync()
    {
        try
        {
            var client =
                _httpClientFactory.CreateClient("BarakahAPI");

            Packages =
                await client.GetFromJsonAsync<List<UmrahPackage>>(
                    "api/Packages"
                ) ?? new List<UmrahPackage>();
        }
        catch (HttpRequestException)
        {
            ErrorMessage =
                "Packages are temporarily unavailable. Please try again later.";
        }
        catch (TaskCanceledException)
        {
            ErrorMessage =
                "The package request timed out. Please try again later.";
        }
        catch (System.Text.Json.JsonException)
        {
            ErrorMessage =
                "Package information could not be loaded.";
        }
    }
}
