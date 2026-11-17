
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

    public class GoogleLoginInput
    {
        public string IdToken { get; set; } = string.Empty;
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

    // POST /Login?handler=Google
    // Called by google-signin.js after the browser signed in with Google (Firebase).
    // The ID token is verified by the API, then the website creates the same customer
    // cookie session that the email/password login creates.
    public async Task<IActionResult> OnPostGoogleAsync(
        [FromBody] GoogleLoginInput input)
    {
        if (string.IsNullOrWhiteSpace(input?.IdToken))
        {
            return GoogleError(400, "Google sign-in failed. Please try again.");
        }

        try
        {
            var client = _httpClientFactory.CreateClient("BarakahAPI");

            using var response = await client.PostAsJsonAsync(
                "api/CustomerAuth/google",
                new { input.IdToken });

            if (response.StatusCode == HttpStatusCode.Forbidden)
            {
                return GoogleError(403,
                    "No customer account uses this Google email. " +
                    "Register with your registration code first, using the same email.");
            }

            if (response.StatusCode == HttpStatusCode.Unauthorized)
            {
                return GoogleError(401,
                    "Google sign-in could not be verified. Please try again.");
            }

            if (!response.IsSuccessStatusCode)
            {
                return GoogleError(503, "Login is temporarily unavailable.");
            }

            var result =
                await response.Content.ReadFromJsonAsync<LoginApiResponse>();

            if (result is null ||
                !result.Success ||
                result.CustomerId <= 0 ||
                string.IsNullOrWhiteSpace(result.Email))
            {
                return GoogleError(401, "Unable to verify your account.");
            }

            // Same claims as the password login, so the rest of the site works unchanged.
            var claims = new List<Claim>
            {
                new Claim(ClaimTypes.NameIdentifier, result.CustomerId.ToString()),
                new Claim(ClaimTypes.Name, result.FirstName ?? "Customer"),
                new Claim(ClaimTypes.Email, result.Email),
                new Claim(ClaimTypes.Role, "Customer")
            };

            var identity = new ClaimsIdentity(
                claims,
                CookieAuthenticationDefaults.AuthenticationScheme);

            await HttpContext.SignInAsync(
                CookieAuthenticationDefaults.AuthenticationScheme,
                new ClaimsPrincipal(identity));

            return new JsonResult(new
            {
                success = true,
                redirectUrl = Url.Page("/CustomerDashboard")
            });
        }
        catch (HttpRequestException)
        {
            return GoogleError(503, "Unable to connect to the server.");
        }
        catch (TaskCanceledException)
        {
            return GoogleError(504, "The login request timed out.");
        }
    }

    // Small helper: JSON error with an HTTP status code for the JavaScript to display.
    private static JsonResult GoogleError(int statusCode, string message)
    {
        return new JsonResult(new { success = false, message })
        {
            StatusCode = statusCode
        };
    }
}
