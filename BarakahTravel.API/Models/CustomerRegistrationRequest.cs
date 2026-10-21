
using System.ComponentModel.DataAnnotations;

namespace BarakahTravel.API.Models;

public class CustomerRegistrationRequest
{
    [Required]
    [StringLength(50)]
    public string RegistrationCode { get; set; } = string.Empty;

    [Required, StringLength(50)]
    public string FirstName { get; set; } = string.Empty;

    [Required, StringLength(50)]
    public string Surname { get; set; } = string.Empty;

    [Required, StringLength(30)]
    public string Phone { get; set; } = string.Empty;


    [Required]
    [EmailAddress]
    [StringLength(100)]
    public string Email { get; set; } = string.Empty;

    [StringLength(30)]
    public string? IDNumber { get; set; }

    [StringLength(30)]
    public string? PassportNumber { get; set; }

    public DateOnly? DateOfBirth { get; set; }

    [StringLength(20)]
    public string? Gender { get; set; }

    [StringLength(255)]
    public string? Address { get; set; }


    [Required]
    [StringLength(128, MinimumLength = 12)]
    public string Password { get; set; } = string.Empty;

    [Required]
    [Compare(nameof(Password))]
    public string ConfirmPassword { get; set; } = string.Empty;

}
