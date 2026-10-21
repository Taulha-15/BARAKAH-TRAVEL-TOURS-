
using System.ComponentModel.DataAnnotations;

namespace BarakahTravel.API.Models;

public class CustomerLoginRequest
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    public string Password { get; set; } = string.Empty;
}

