using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

public partial class TravelDetail
{
    [Key]
    [Column("TravelDetailID")]
    public int TravelDetailId { get; set; }

    [Column("ApplicationID")]
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

    [ForeignKey("ApplicationId")]
    [InverseProperty("TravelDetails")]
    public virtual Application Application { get; set; } = null!;
}
