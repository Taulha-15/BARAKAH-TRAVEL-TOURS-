
using System.ComponentModel.DataAnnotations;

namespace BarakahTravel.API.Models;

public class CreateApplicationRequest
{
    [Range(1, int.MaxValue)]
    public int PackageId { get; set; }

    [StringLength(1000)]
    public string? Notes { get; set; }
}
