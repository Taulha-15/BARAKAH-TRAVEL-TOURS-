
namespace BarakahTravel.Web.Models;

public class UmrahPackage
{
    public int PackageId { get; set; }

    public string PackageName { get; set; } = string.Empty;

    public string? Description { get; set; }

    public decimal Price { get; set; }

    public int DurationDays { get; set; }

    public DateOnly DepartureDate { get; set; }

    public DateOnly ReturnDate { get; set; }
}
