using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;
using Microsoft.EntityFrameworkCore;

namespace BarakahTravel.API.Models;

public partial class Document
{
    [Key]
    [Column("DocumentID")]
    public int DocumentId { get; set; }

    [Column("ApplicationID")]
    public int ApplicationId { get; set; }

    [StringLength(50)]
    public string DocumentType { get; set; } = null!;

    [StringLength(255)]
    public string FileName { get; set; } = null!;

    [StringLength(500)]
    public string? FileUrl { get; set; }

    public DateTime UploadedAt { get; set; }

    [StringLength(30)]
    public string VerificationStatus { get; set; } = null!;

    public int? VerifiedBy { get; set; }

    public DateTime? VerifiedAt { get; set; }

    [ForeignKey("ApplicationId")]
    [InverseProperty("Documents")]
    public virtual Application Application { get; set; } = null!;

    [ForeignKey("VerifiedBy")]
    [InverseProperty("Documents")]
    public virtual User? VerifiedByNavigation { get; set; }
}
