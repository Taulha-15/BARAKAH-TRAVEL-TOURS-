
using System.ComponentModel.DataAnnotations;

namespace BarakahTravel.Web.Models;

public class CustomerRegistrationInput
{
    [Required(ErrorMessage = "Registration code is required.")]
    [StringLength(50)]
    public string RegistrationCode { get; set; } = string.Empty;

    [Required(ErrorMessage = "First name is required.")]
    [StringLength(50)]
    public string FirstName { get; set; } = string.Empty;

    [Required(ErrorMessage = "Surname is required.")]
    [StringLength(50)]
    public string Surname { get; set; } = string.Empty;

    [Required(ErrorMessage = "Email address is required.")]
    [EmailAddress]
    [StringLength(100)]
    public string Email { get; set; } = string.Empty;

    [Required(ErrorMessage = "Phone number is required.")]
    [StringLength(30)]
    public string Phone { get; set; } = string.Empty;

    [Required(ErrorMessage = "Password is required.")]
    [StringLength(128, MinimumLength = 12)]
    [DataType(DataType.Password)]
    public string Password { get; set; } = string.Empty;

    [Required(ErrorMessage = "Please confirm your password.")]
    [Compare(nameof(Password),
        ErrorMessage = "Passwords do not match.")]
    [DataType(DataType.Password)]
    public string ConfirmPassword { get; set; } = string.Empty;
}
