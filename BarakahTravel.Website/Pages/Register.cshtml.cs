
using System.Net;
using System.Net.Http.Json;
using System.Security.Claims;
using BarakahTravel.Web.Models;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.Cookies;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace BarakahTravel.Web.Pages;

public class RegisterModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public RegisterModel(IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    [BindProperty]
    public CustomerRegistrationInput Input { get; set; } = new();

    public string? SuccessMessage { get; set; }

    public async Task<IActionResult> OnPostAsync()
    {
        if (!ModelState.IsValid)
        {
            return Page();
        }

        try
        {
            var client =
                _httpClientFactory.CreateClient("BarakahAPI");

            // STEP 1: Register the customer.
            using var registrationResponse =
                await client.PostAsJsonAsync(
                    "api/CustomerRegistration/register",
                    Input);

            if (!registrationResponse.IsSuccessStatusCode)
            {
                ModelState.AddModelError(
                    string.Empty,
                    registrationResponse.StatusCode == HttpStatusCode.BadRequest
                        ? "Registration could not be completed. Check your details and registration code."
                        : "Registration is temporarily unavailable.");

                ClearPasswords();
                return Page();
            }

            // STEP 2: Verify the newly created account.
            using var loginResponse =
                await client.PostAsJsonAsync(
                    "api/CustomerAuth/login",
                    new
                    {
                        Input.Email,
                        Input.Password
                    });

            var loginResult = await loginResponse.Content
                .ReadFromJsonAsync<LoginModel.LoginApiResponse>();

            if (loginResult is null ||
                !loginResult.Success ||
                loginResult.CustomerId <= 0 ||
                string.IsNullOrWhiteSpace(loginResult.Email))
            {
                ModelState.AddModelError(
                    "",
                    "Your account was created, but automatic sign-in could not be completed. Please use the Login page.");

                ClearPasswords();
                return Page();
            }


            if (!loginResponse.IsSuccessStatusCode)
            {
                ModelState.AddModelError(
                    string.Empty,
                    "Your account was created, but automatic sign-in could not be completed. Please use the Login page.");

                ClearPasswords();
                return Page();
            }

            // STEP 3: Establish the website login session.

            var claims = new List<Claim>
{
    new Claim(
        ClaimTypes.NameIdentifier,
        loginResult.CustomerId.ToString()),

    new Claim(
        ClaimTypes.Name,
        loginResult.FirstName ?? "Customer"),

    new Claim(
        ClaimTypes.Email,
        loginResult.Email),

    new Claim(
        ClaimTypes.Role,
        "Customer")
};


            // STEP 4: Open the Customer Portal.
            return RedirectToPage("/CustomerDashboard");
        }
        catch (HttpRequestException)
        {
            ModelState.AddModelError(
                string.Empty,
                "Unable to connect to the registration service. Please try again.");

            ClearPasswords();
            return Page();
        }
        catch (TaskCanceledException)
        {
            ModelState.AddModelError(
                string.Empty,
                "The request timed out. Please check whether your account was created before trying again.");

            ClearPasswords();
            return Page();
        }
    }

    private void ClearPasswords()
    {
        Input.Password = string.Empty;
        Input.ConfirmPassword = string.Empty;

        ModelState.Remove("Input.Password");
        ModelState.Remove("Input.ConfirmPassword");
    }
}
