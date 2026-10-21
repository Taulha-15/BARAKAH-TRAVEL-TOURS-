
using System.ComponentModel.DataAnnotations;

namespace BarakahTravel.API.Models;

public class CreateTravelDetailsRequest : IValidatableObject
{
    [Range(1, int.MaxValue)]
    public int ApplicationId { get; set; }

    [StringLength(100)]
    public string? DepartureCity { get; set; }

    [StringLength(100)]
    public string? Destination { get; set; }

    public DateOnly? DepartureDate { get; set; }

    public DateOnly? ReturnDate { get; set; }

    [StringLength(100)]
    public string? Airline { get; set; }

    [StringLength(50)]
    public string? FlightNumber { get; set; }

    [StringLength(500)]
    public string? AdditionalNotes { get; set; }

    public IEnumerable<ValidationResult> Validate(
        ValidationContext validationContext)
    {
        if (DepartureDate.HasValue &&
            ReturnDate.HasValue &&
            ReturnDate.Value < DepartureDate.Value)
        {
            yield return new ValidationResult(
                "Return date cannot be earlier than departure date.",
                new[] { nameof(ReturnDate) });
        }
    }
}
