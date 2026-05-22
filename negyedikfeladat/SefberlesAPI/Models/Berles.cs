using System.ComponentModel.DataAnnotations.Schema;

namespace SefberlesAPI.Models
{
    public class Berles
    {
        public int Id { get; set; }
        public int Uid { get; set; }
        public int ChefId { get; set; }
        public DateTime StartDate { get; set; }
        public DateTime EndDate { get; set; }
        public int DailyRate { get; set; }
        public int BaseFee { get; set; }

        [NotMapped]
        public int TotalPrice => BaseFee + (int)(EndDate - StartDate).TotalDays * DailyRate;
    }
}
