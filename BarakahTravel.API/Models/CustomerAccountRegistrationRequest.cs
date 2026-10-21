
using System.ComponentModel.DataAnnotations;

namespace BarakahTravel.API.Models
{
    public class CustomerAccountRegistrationRequest
    {
        [Required]
        [StringLength(50)]
        public string RegistrationCode { get; set; } = string.Empty;

        [Required]
        [EmailAddress]
        [StringLength(100)]
        public string Email { get; set; } = string.Empty;

        [Required]
        [MinLength(12)]
        [MaxLength(128)]
        public string Password { get; set; } = string.Empty;

        [Required]
        [Compare(nameof(Password))]
        public string ConfirmPassword { get; set; } = string.Empty;
    }
}

