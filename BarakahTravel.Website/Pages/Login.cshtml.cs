
using System.ComponentModel.DataAnnotations;
using System.Net;
using System.Net.Http.Json;
using System.Security.Claims;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace BarakahTravel.Web.Pages;

public class LoginModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public LoginModel(IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    [BindProperty]
    public LoginInput Input { get; set; } = new();

    public class LoginInput
    {
        [Required]
        [EmailAddress]
        public string Email { get; set; } = string.Empty;

        [Required]
        [DataType(DataType.Password)]
        public string Password { get; set; } = string.Empty;
    }

    public class LoginApiResponse
    {
        public bool Success { get; set; }
        public int CustomerId { get; set; }
        public string? FirstName { get; set; }
        public string? Email { get; set; }
    }

    public IActionResult OnGet()
    {
        if (User.Identity?.IsAuthenticated == true)
            return RedirectToPage("/CustomerDashboard");

        return Page();
    }

    public async Task<IActionResult> OnPostAsync()
    {
        if (!ModelState.IsValid)
            return Page();

        try
        {
            var client = _httpClientFactory.CreateClient("BarakahAPI");

            using var response = await client.PostAsJsonAsync(
                "api/CustomerAuth/login",
                new
                {
                    Input.Email,
                    Input.Password
                });

            if (response.StatusCode == HttpStatusCode.Unauthorized)
            {
                ModelState.AddModelError(
                    "",
                    "Incorrect email or password.");

                Input.Password = string.Empty;
                return Page();
            }

            if (!response.IsSuccessStatusCode)
            {
                ModelState.AddModelError(
                    "",
                    "Login is temporarily unavailable.");

                Input.Password = string.Empty;
                return Page();
            }

            var result =
                await response.Content.ReadFromJsonAsync<LoginApiResponse>();

            if (result is null ||
                !result.Success ||
                result.CustomerId <= 0 ||
                string.IsNullOrWhiteSpace(result.Email))
            {
                ModelState.AddModelError(
                    "",
                    "Unable to verify your account.");

                Input.Password = string.Empty;
                return Page();
            }

            var claims = new List<Claim>
            {
                new Claim(
                    ClaimTypes.NameIdentifier,
                    result.CustomerId.ToString()),

                new Claim(
                    ClaimTypes.Name,
                    result.FirstName ?? "Customer"),

                new Claim(
                    ClaimTypes.Email,
                    result.Email),

                new Claim(
                    ClaimTypes.Role,
                    "Customer")
            };

            var identity = new ClaimsIdentity(
                claims,
                CookieAuthenticationDefaults.AuthenticationScheme);

            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                new ClaimsPrincipal(identity));

            return RedirectToPage("/CustomerDashboard");
        }
        catch (HttpRequestException)
        {
            ModelState.AddModelError(
                "",
                "Unable to connect to the server.");
        }
        catch (TaskCanceledException)
        {
            ModelState.AddModelError(
                "",
                "The login request timed out.");
        }

        Input.Password = string.Empty;
        return Page();
    }
}
