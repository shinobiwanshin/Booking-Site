import { TicketDetails, TicketStatus } from "@/domain/domain";
import { getTicket, getTicketQr } from "@/lib/api";
import { format } from "date-fns";
import { Calendar, DollarSign, MapPin, Tag, Download } from "lucide-react";
import { useEffect, useState, useRef } from "react";
import { useAuth } from "react-oidc-context";
import { useParams } from "react-router";
import jsPDF from "jspdf";
import domtoimage from "dom-to-image-more"; // Using dom-to-image-more

const DashboardViewTicketPage: React.FC = () => {
  const [ticket, setTicket] = useState<TicketDetails | undefined>();
  const [qrCodeUrl, setQrCodeUrl] = useState<string | undefined>();
  const [isQrLoading, setIsQrCodeLoading] = useState(true);
  const [error, setError] = useState<string | undefined>();

  const { id } = useParams();
  const { isLoading, user } = useAuth();
  const ticketRef = useRef<HTMLDivElement>(null);

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
    if (!ticket) return;

    // We replace all complex React/Tailwind rendering with a simple HTML string
    // using only guaranteed supported CSS (hex, rgb, basic properties).
    const ticketHtml = `
      <div style="
        width: 350px; 
        padding: 30px; 
        margin: 20px auto; 
        background-color: #4C1D95; 
        color: #F5F5F5; 
        border-radius: 20px; 
        box-shadow: 0 10px 15px rgba(0, 0, 0, 0.5);
        font-family: Inter, Arial, sans-serif;
      ">
        
        <div style="text-align: center; margin-bottom: 25px;">
          <span style="
            font-size: 14px; 
            font-weight: bold; 
            padding: 4px 12px; 
            border-radius: 9999px; /* Rounded-full */
            background-color: #222222; /* Solid dark background */
            color: #D1C4E9; /* Light purple text color */
          ">
            ${ticket.status}
          </span>
        </div>

        <h1 style="font-size: 24px; font-weight: bold; margin-bottom: 8px;">
          ${ticket.eventName}
        </h1>
        
        <p style="margin-bottom: 5px; font-size: 14px; color: #D1C4E9;">
          📍 ${ticket.eventVenue}
        </p>
        
        <p style="margin-bottom: 25px; font-size: 14px; color: #D1C4E9;">
          📅 ${format(ticket.eventStart, "Pp")} - ${format(ticket.eventEnd, "Pp")}
        </p>

        <div style="text-align: center; margin-bottom: 20px;">
          <img 
            src="${qrCodeUrl || ""}" 
            alt="QR Code" 
            style="width: 128px; height: 128px; padding: 10px; background-color: white; border-radius: 10px;"
          />
          <p style="font-size: 12px; color: #D1C4E9; margin-top: 10px;">
            Present this QR code at the venue for entry
          </p>
        </div>

        <div style="margin-top: 15px; border-top: 1px solid rgba(255, 255, 255, 0.1); padding-top: 15px;">
          <p style="font-size: 14px; margin-bottom: 5px;">
            🏷️ <span style="font-weight: bold;">${ticket.description}</span>
          </p>
          <p style="font-size: 16px; font-weight: bold;">
            💵 ${ticket.price}
          </p>
        </div>
        
        <div style="text-align: center; margin-top: 20px; padding-top: 10px;">
          <h4 style="font-size: 12px; font-weight: bold; font-family: monospace;">Ticket ID</h4>
          <p style="font-size: 12px; color: #D1C4E9; font-family: monospace;">${ticket.id}</p>
        </div>
      </div>
    `;

    // 1. Create a temporary element container
    const tempContainer = document.createElement("div");
    tempContainer.innerHTML = ticketHtml;

    // 2. Safely extract the first ELEMENT (the main ticket div)
    const elementToCapture =
      tempContainer.firstElementChild as HTMLElement | null;

    if (!elementToCapture) {
      console.error("Failed to construct the printable HTML element.");
      return;
    }

    // 3. Position the element off-screen for capture
    elementToCapture.style.position = "absolute";
    elementToCapture.style.top = "-9999px";
    document.body.appendChild(elementToCapture);

    const scale = 3;

    try {
      // 4. Convert HTML to PNG (using dom-to-image-more)
      const dataUrl = await domtoimage.toPng(elementToCapture, {
        width: elementToCapture.offsetWidth * scale,
        height: elementToCapture.offsetHeight * scale,
        style: {
          transform: `scale(${scale})`,
          transformOrigin: "top left",
        },
      });

      // 5. Convert PNG to PDF
      const pdf = new jsPDF({
        orientation: "portrait",
        unit: "pt",
        format: "a4",
      });
      const imgProps = pdf.getImageProperties(dataUrl);
      const imageNativeWidth = imgProps.width / scale;
      const imageNativeHeight = imgProps.height / scale;
      const pdfWidth = pdf.internal.pageSize.getWidth();
      const margin = 40;
      const contentWidth = pdfWidth - 2 * margin;
      const contentHeight =
        (imageNativeHeight * contentWidth) / imageNativeWidth;
      const xPosition = margin;
      const yPosition = margin;

      pdf.addImage(
        dataUrl,
        "PNG",
        xPosition,
        yPosition,
        contentWidth,
        contentHeight,
      );
      pdf.save(`${ticket.eventName || "ticket"}.pdf`);
    } catch (err) {
      console.error("Error generating PDF:", err);
    } finally {
      // 6. CRITICAL: Clean up the temporary element
      document.body.removeChild(elementToCapture);
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
      <div ref={ticketRef} className="w-full max-w-md">
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
