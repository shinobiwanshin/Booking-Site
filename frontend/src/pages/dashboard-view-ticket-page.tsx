import { TicketDetails, TicketStatus } from "@/domain/domain";
import { getTicket, getTicketQr, getTicketPdf } from "@/lib/api";
import { format } from "date-fns";
import { Calendar, DollarSign, MapPin, Tag, Download } from "lucide-react";
import { useEffect, useState } from "react";
import { useAuth } from "react-oidc-context";
import { useParams } from "react-router";

const DashboardViewTicketPage: React.FC = () => {
  const [ticket, setTicket] = useState<TicketDetails | undefined>();
  const [qrCodeUrl, setQrCodeUrl] = useState<string | undefined>();
  const [isQrLoading, setIsQrCodeLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();

  const { id } = useParams();
  const { isLoading, user } = useAuth();

  useEffect(() => {
    if (isLoading || !user?.access_token || !id) return;

    const fetchData = async (accessToken: string, id: string) => {
      try {
        setIsQrCodeLoading(true);
        setError(undefined);

        const ticketData = await getTicket(accessToken, id);
        const qrBlob = await getTicketQr(accessToken, id);

        setTicket(ticketData);
        setQrCodeUrl(URL.createObjectURL(qrBlob));
      } catch (err) {
        if (err instanceof Error) setError(err.message);
        else if (typeof err === "string") setError(err);
        else setError("An unknown error has occurred");
      } finally {
        setIsQrCodeLoading(false);
      }
    };

    fetchData(user.access_token, id);

    return () => {
      if (qrCodeUrl) URL.revokeObjectURL(qrCodeUrl);
    };
  }, [user?.access_token, isLoading, id]);

  // NOTE: getStatusColor is no longer used in the PDF generation, but kept for the UI.
  const getStatusColor = (status: TicketStatus) => {
    switch (status) {
      case TicketStatus.PURCHASED:
        return "text-green-400";
      case TicketStatus.CANCELLED:
        return "text-red-400";
      default:
        return "text-gray-400";
    }
  };

  const downloadPdf = async () => {
    if (!ticket || isLoading || !user?.access_token) return;

    try {
      const blob = await getTicketPdf(user.access_token, ticket.id);
      const url = URL.createObjectURL(blob);
      const a = document.createElement("a");
      a.href = url;
      a.download = `${ticket.eventName || "ticket"}.pdf`;
      document.body.appendChild(a);
      a.click();
      a.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      console.error(err);
      alert(
        err instanceof Error ? err.message : "Failed to download ticket PDF",
      );
    }
  };

  if (!ticket) return <p>Loading..</p>;

  return (
    <div className="bg-black min-h-screen text-white flex flex-col items-center justify-center p-4">
      {/* PDF Download Button */}
      <button
        onClick={downloadPdf}
        className="mb-4 flex items-center gap-2 bg-purple-700 hover:bg-purple-800 px-4 py-2 rounded-lg font-semibold transition"
      >
        <Download className="w-4" /> Download Ticket as PDF
      </button>

      {/* Ticket content (This is the original UI display) */}
      <div className="w-full max-w-md">
        <div className="relative bg-gradient-to-br from-purple-900 via-purple-800 to-indigo-900 rounded-3xl p-8 shadow-2xl">
          {/* Status */}
          <div className="bg-black/30 backdrop-blur-sm px-3 py-1 rounded-full mb-8 text-center">
            <span
              className={`text-sm font-medium ${getStatusColor(ticket.status)}`}
            >
              {ticket.status}
            </span>
          </div>

          {/* Event info */}
          <div className="mb-2">
            <h1 className="text-2xl font-bold mb-2">{ticket.eventName}</h1>
            <div className="flex items-center gap-2 text-purple-200">
              <MapPin className="w-4" />
              <span>{ticket.eventVenue}</span>
            </div>
          </div>

          <div className="flex items-center gap-2 text-purple-300 mb-8">
            <Calendar className="w-4 text-purple-200" />
            <div>
              {format(ticket.eventStart, "Pp")} -{" "}
              {format(ticket.eventEnd, "Pp")}
            </div>
          </div>

          {/* QR Section */}
          <div className="flex justify-center mb-8">
            <div className="bg-white p-4 rounded-2xl shadow-lg">
              <div className="w-32 h-32 flex items-center justify-center">
                {isQrLoading && (
                  <div className="text-xs text-center p2">
                    <div className="animate-spin rounded-full h-8 w-8 border-b-2 mb-2 mx-auto"></div>
                    <div className="text-gray-800">Loading QR...</div>
                  </div>
                )}
                {error && (
                  <div className="text-red-400 text-sm text-center p-2">
                    <div className="mb-1">⚠️</div>
                    {error}
                  </div>
                )}
                {qrCodeUrl && !isQrLoading && !error && (
                  <img
                    src={qrCodeUrl}
                    alt="QR Code for event"
                    className="w-full h-full object-contain rounded-large"
                  />
                )}
              </div>
            </div>
          </div>

          <div className="text-center mb-8">
            <p className="text-purple-200 text-sm">
              Present this QR code at the venue for entry
            </p>
          </div>

          {/* Description + Price */}
          <div className="space-y-2 mb-8">
            <div className="flex items-center gap-2">
              <Tag className="w-5 text-purple-200" />
              <span className="font-semibold">{ticket.description}</span>
            </div>
            <div className="flex items-center gap-2">
              <DollarSign className="w-5 text-purple-200" />
              <span className="font-semibold">{ticket.price}</span>
            </div>
          </div>

          {/* Ticket ID */}
          <div className="text-center mb-2">
            <h4 className="text-sm font-semibold font-mono">Ticket ID</h4>
            <p className="text-purple-200 text-sm font-mono">{ticket.id}</p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default DashboardViewTicketPage;
