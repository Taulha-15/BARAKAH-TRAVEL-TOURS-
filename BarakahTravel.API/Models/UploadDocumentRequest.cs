
using System.ComponentModel.DataAnnotations;
using Microsoft.AspNetCore.Http;

namespace BarakahTravel.API.Models;

public class UploadDocumentRequest
{
    [Range(1, int.MaxValue)]
    public int ApplicationId { get; set; }

    [Required]
    [StringLength(50)]
    public string DocumentType { get; set; } = string.Empty;

    [Required]
    public IFormFile File { get; set; } = null!;
}
