
using System.ComponentModel.DataAnnotations;
using System.Net;
using System.Net.Http.Json;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.RazorPages;

namespace BarakahTravel.Web.Pages;

[Authorize(Policy = "CustomerOnly")]
public class PersonalDetailsModel : PageModel
{
    private readonly IHttpClientFactory _httpClientFactory;

    public PersonalDetailsModel(
        IHttpClientFactory httpClientFactory)
    {
        _httpClientFactory = httpClientFactory;
    }

    public CustomerDetails? Customer { get; set; }

    [BindProperty]
    public EditPersonalDetails EditDetails { get; set; } = new();

    public string? ErrorMessage { get; set; }

    public async Task OnGetAsync()
    {
        await LoadCustomerAsync();

        if (Customer != null)
        {
            EditDetails = new EditPersonalDetails
            {
                FirstName = Customer.FirstName ?? "",
                Surname = Customer.Surname ?? "",
                Phone = Customer.Phone ?? "",
                Idnumber = Customer.Idnumber,
                PassportNumber = Customer.PassportNumber,
                DateOfBirth = Customer.DateOfBirth,
                Gender = Customer.Gender,
                Address = Customer.Address
            };
        }
    }

    public async Task<IActionResult> OnPostSaveAsync()
    {
        // Keep the submitted form values if validation fails.
        if (!ModelState.IsValid)
        {
            await LoadCustomerAsync();
            return Page();
        }

        if (!Request.Cookies.TryGetValue(
            "BarakahTravel.Web.Customer",
            out var customerCookie))
        {
            return RedirectToPage("/Login");
        }

        try
        {
            var client = _httpClientFactory
                .CreateClient("BarakahAPI");

            using var request = new HttpRequestMessage(
                HttpMethod.Put,
                "api/Customers/me");

            request.Headers.TryAddWithoutValidation(
                "Cookie",
                $"BarakahTravel.Web.Customer={customerCookie}");

            request.Headers.Add(
    "X-Barakah-Portal",
    "PersonalDetails");

            request.Content = JsonContent.Create(EditDetails);

            using var response = await client.SendAsync(request);

            if (response.StatusCode == HttpStatusCode.Unauthorized ||
                response.StatusCode == HttpStatusCode.Forbidden)
            {
                ErrorMessage =
                    "Your session has expired. Please log in again.";

                await LoadCustomerAsync();
                return Page();
            }

            if (!response.IsSuccessStatusCode)
            {
                ErrorMessage =
                    "Your changes could not be saved. Please check your details.";

                await LoadCustomerAsync();
                return Page();
            }

            TempData["SuccessMessage"] =
                "Your personal details have been updated successfully.";

            return RedirectToPage("/PersonalDetails");
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

        await LoadCustomerAsync();
        return Page();
    }

    private async Task LoadCustomerAsync()
    {
        try
        {
            if (!Request.Cookies.TryGetValue(
                "BarakahTravel.Web.Customer",
                out var customerCookie))
            {
                ErrorMessage = "Please log in again.";
                return;
            }

            var client = _httpClientFactory
                .CreateClient("BarakahAPI");

            using var request = new HttpRequestMessage(
                HttpMethod.Get,
                "api/Customers/me");

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
                    "Unable to load your personal details.";
                return;
            }

            Customer = await response.Content
                .ReadFromJsonAsync<CustomerDetails>();
        }
        catch (HttpRequestException)
        {
            ErrorMessage = "Unable to connect to the server.";
        }
        catch (TaskCanceledException)
        {
            ErrorMessage = "The request timed out.";
        }
    }

    public class CustomerDetails
    {
        public int CustomerId { get; set; }
        public string? FirstName { get; set; }
        public string? Surname { get; set; }
        public string? Email { get; set; }
        public string? Phone { get; set; }
        public string? Idnumber { get; set; }
        public string? PassportNumber { get; set; }
        public DateOnly? DateOfBirth { get; set; }
        public string? Gender { get; set; }
        public string? Address { get; set; }
    }

    public class EditPersonalDetails
    {
        [Required]
        [StringLength(50)]
        public string FirstName { get; set; } = "";

        [Required]
        [StringLength(50)]
        public string Surname { get; set; } = "";

        [Required]
        [StringLength(30)]
        public string Phone { get; set; } = "";

        [StringLength(30)]
        public string? Idnumber { get; set; }

        [StringLength(30)]
        public string? PassportNumber { get; set; }

        [DataType(DataType.Date)]
        public DateOnly? DateOfBirth { get; set; }

        [StringLength(20)]
        public string? Gender { get; set; }

        [StringLength(255)]
        public string? Address { get; set; }
    }
}
