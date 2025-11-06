import { useEffect, useState } from "react";
import { useNavigate } from "react-router";
import { useAuth } from "@/hooks/use-auth";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import {
  CheckCircle,
  Ticket,
  Calendar,
  Users,
  CreditCard,
  BarChart,
  Shield,
  Sparkles,
} from "lucide-react";

export default function WelcomeOnboardingPage() {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [userRole, setUserRole] = useState<"ATTENDEE" | "ORGANIZER">(
    "ATTENDEE",
  );
  const [userName, setUserName] = useState<string>("");

  useEffect(() => {
    // Check if user is new and get their role
    const isNewUser = localStorage.getItem("isNewUser");
    const role =
      (localStorage.getItem("userRole") as "ATTENDEE" | "ORGANIZER") ||
      "ATTENDEE";

    if (!isNewUser) {
      // Not a new user, redirect to appropriate page
      navigate("/");
      return;
    }

    setUserRole(role);

    // Get user name from auth
    if (user) {
      setUserName(user.username || "there");
    }
  }, [user, navigate]);

  const handleGetStarted = () => {
    // Clear the new user flag
    localStorage.removeItem("isNewUser");
    localStorage.removeItem("userRole");

    // Redirect based on role
    if (userRole === "ORGANIZER") {
      navigate("/organizers");
    } else {
      navigate("/");
    }
  };

  const attendeeFeatures = [
    {
      icon: <Ticket className="h-6 w-6 text-purple-500" />,
      title: "Browse Events",
      description: "Discover exciting events happening near you or online",
    },
    {
      icon: <CreditCard className="h-6 w-6 text-purple-500" />,
      title: "Easy Purchasing",
      description: "Buy tickets securely with just a few clicks",
    },
    {
      icon: <Shield className="h-6 w-6 text-purple-500" />,
      title: "Digital Tickets",
      description: "Get instant access to your tickets with QR codes",
    },
    {
      icon: <Users className="h-6 w-6 text-purple-500" />,
      title: "Manage Orders",
      description: "View and track all your ticket purchases in one place",
    },
  ];

  const organizerFeatures = [
    {
      icon: <Calendar className="h-6 w-6 text-purple-500" />,
      title: "Create Events",
      description: "Set up and publish events with custom ticket types",
    },
    {
      icon: <Ticket className="h-6 w-6 text-purple-500" />,
      title: "Manage Tickets",
      description: "Control ticket availability, pricing, and sales periods",
    },
    {
      icon: <BarChart className="h-6 w-6 text-purple-500" />,
      title: "Track Performance",
      description: "View real-time sales data and revenue analytics",
    },
    {
      icon: <Shield className="h-6 w-6 text-purple-500" />,
      title: "Validate Entry",
      description: "Scan QR codes to verify tickets at your event",
    },
  ];

  const features =
    userRole === "ATTENDEE" ? attendeeFeatures : organizerFeatures;

  if (!isAuthenticated) {
    return (
      <div className="min-h-screen bg-black text-white flex items-center justify-center">
        <p>Loading...</p>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-black text-white">
      <div className="container mx-auto px-4 py-16 max-w-4xl">
        {/* Welcome Header */}
        <div className="text-center mb-12">
          <div className="flex justify-center mb-4">
            <div className="bg-purple-900/30 p-4 rounded-full">
              <Sparkles className="h-12 w-12 text-purple-500" />
            </div>
          </div>
          <h1 className="text-4xl font-bold mb-4">
            Welcome{userName ? `, ${userName}` : ""}! 🎉
          </h1>
          <p className="text-xl text-gray-400 mb-2">
            Your account has been created successfully
          </p>
          <p className="text-lg text-gray-500">
            {userRole === "ATTENDEE"
              ? "Let's help you find amazing events to attend"
              : "Let's get you started with organizing your first event"}
          </p>
        </div>

        {/* Features Grid */}
        <div className="grid md:grid-cols-2 gap-6 mb-12">
          {features.map((feature, index) => (
            <Card key={index} className="bg-gray-900 border-gray-800">
              <CardHeader>
                <div className="flex items-center gap-3">
                  {feature.icon}
                  <CardTitle className="text-lg text-white">
                    {feature.title}
                  </CardTitle>
                </div>
              </CardHeader>
              <CardContent>
                <CardDescription className="text-gray-400">
                  {feature.description}
                </CardDescription>
              </CardContent>
            </Card>
          ))}
        </div>

        {/* Quick Tips */}
        <Card className="bg-gradient-to-r from-purple-900/20 to-pink-900/20 border-purple-700/50 mb-8">
          <CardHeader>
            <CardTitle className="text-white flex items-center gap-2">
              <CheckCircle className="h-5 w-5 text-green-500" />
              Quick Tips
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-2 text-gray-300">
            {userRole === "ATTENDEE" ? (
              <>
                <p>• Use the search bar to find events by name or keyword</p>
                <p>• Check the dashboard to view all your purchased tickets</p>
                <p>• Download your tickets as PDFs for offline access</p>
                <p>
                  • Show the QR code at the event entrance for quick check-in
                </p>
              </>
            ) : (
              <>
                <p>• Click "Create Event" to set up your first event</p>
                <p>• Add multiple ticket types with different prices</p>
                <p>
                  • Set sales start/end dates to control ticket availability
                </p>
                <p>
                  • Use the dashboard to monitor sales and revenue in real-time
                </p>
                <p>• Validate tickets at your event using the QR scanner</p>
              </>
            )}
          </CardContent>
        </Card>

        {/* CTA Button */}
        <div className="text-center">
          <Button
            onClick={handleGetStarted}
            className="bg-purple-600 hover:bg-purple-700 text-lg px-8 py-6"
            size="lg"
          >
            {userRole === "ATTENDEE"
              ? "Start Browsing Events"
              : "Go to Dashboard"}
          </Button>
        </div>
      </div>
    </div>
  );
}
