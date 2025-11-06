import NavBar from "@/components/nav-bar";
import { Card, CardContent, CardHeader } from "@/components/ui/card";
import { useEffect, useState } from "react";
import { useAuth } from "@/hooks/use-auth";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert";
import {
  AlertCircle,
  BarChart2,
  DollarSign,
  Ticket,
  Users,
} from "lucide-react";
import {
  getOrganizerDashboardSummary,
  type OrganizerDashboardSummary,
} from "@/lib/api";
import { Pie, PieChart, Cell, ResponsiveContainer, Tooltip } from "recharts";

type DashboardSummary = OrganizerDashboardSummary;

const DashboardInsightsPage: React.FC = () => {
  const { isLoading, user } = useAuth();
  const [summary, setSummary] = useState<DashboardSummary | undefined>();
  const [error, setError] = useState<string | undefined>();

  useEffect(() => {
    if (isLoading || !user?.accessToken) return;
    const load = async () => {
      try {
        setSummary(await getOrganizerDashboardSummary(user.accessToken));
      } catch (e) {
        setError(e instanceof Error ? e.message : "Unknown error");
      }
    };
    load();
  }, [isLoading, user?.accessToken]);

  if (error) {
    return (
      <div className="bg-black min-h-screen text-white">
        <NavBar />
        <Alert variant="destructive" className="bg-gray-900 border-red-700">
          <AlertCircle className="h-4 w-4" />
          <AlertTitle>Error</AlertTitle>
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      </div>
    );
  }

  return (
    <div className="bg-black min-h-screen text-white">
      <NavBar />
      <div className="py-8 px-4">
        <h1 className="text-2xl font-bold">Organizer Insights</h1>
        <p>High-level overview of your events and tickets</p>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 px-4">
        <Card className="bg-gray-900 text-white">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <span className="text-sm text-gray-400">Events</span>
            <BarChart2 className="h-5 w-5 text-gray-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {summary?.totalEvents ?? "–"}
            </div>
            <p className="text-xs text-gray-400">
              {summary &&
                `${summary.publishedEvents} published • ${summary.draftEvents} draft`}
            </p>
          </CardContent>
        </Card>

        <Card className="bg-gray-900 text-white">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <span className="text-sm text-gray-400">Tickets Sold</span>
            <Ticket className="h-5 w-5 text-gray-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {summary?.totalTicketsSold ?? "–"}
            </div>
            <p className="text-xs text-gray-400">
              {summary && `${summary.totalTicketsAvailable} total capacity`}
            </p>
          </CardContent>
        </Card>

        <Card className="bg-gray-900 text-white">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <span className="text-sm text-gray-400">Revenue</span>
            <DollarSign className="h-5 w-5 text-gray-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              ${summary?.totalRevenue?.toFixed(2) ?? "–"}
            </div>
            <p className="text-xs text-gray-400">Sum of sold tickets</p>
          </CardContent>
        </Card>

        <Card className="bg-gray-900 text-white">
          <CardHeader className="flex flex-row items-center justify-between pb-2">
            <span className="text-sm text-gray-400">Attendances</span>
            <Users className="h-5 w-5 text-gray-400" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {summary?.totalAttendances ?? "–"}
            </div>
            <p className="text-xs text-gray-400">Validated tickets</p>
          </CardContent>
        </Card>
      </div>

      {/* Charts inspired by admin-dashboard */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 px-4 py-6">
        <Card className="bg-gray-900 text-white">
          <CardHeader>
            <span className="text-sm text-gray-400">Sold vs Capacity</span>
          </CardHeader>
          <CardContent className="h-64">
            {summary && (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Tooltip
                    contentStyle={{
                      background: "#111827",
                      border: "1px solid #374151",
                      color: "#ffffff",
                    }}
                    labelStyle={{ color: "#ffffff" }}
                    itemStyle={{ color: "#ffffff" }}
                  />
                  <Pie
                    data={[
                      { name: "Sold", value: summary.totalTicketsSold },
                      {
                        name: "Remaining",
                        value: Math.max(
                          (summary.totalTicketsAvailable ?? 0) -
                            (summary.totalTicketsSold ?? 0),
                          0,
                        ),
                      },
                    ]}
                    dataKey="value"
                    nameKey="name"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={4}
                  >
                    <Cell fill="#10b981" />
                    <Cell fill="#3b82f6" />
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>

        <Card className="bg-gray-900 text-white">
          <CardHeader>
            <span className="text-sm text-gray-400">Attendance vs Sold</span>
          </CardHeader>
          <CardContent className="h-64">
            {summary && (
              <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Tooltip
                    contentStyle={{
                      background: "#111827",
                      border: "1px solid #374151",
                      color: "#ffffff",
                    }}
                    labelStyle={{ color: "#ffffff" }}
                    itemStyle={{ color: "#ffffff" }}
                  />
                  <Pie
                    data={[
                      { name: "Attended", value: summary.totalAttendances },
                      {
                        name: "Not yet",
                        value: Math.max(
                          (summary.totalTicketsSold ?? 0) -
                            (summary.totalAttendances ?? 0),
                          0,
                        ),
                      },
                    ]}
                    dataKey="value"
                    nameKey="name"
                    innerRadius={60}
                    outerRadius={90}
                    paddingAngle={4}
                  >
                    <Cell fill="#f59e0b" />
                    <Cell fill="#6b7280" />
                  </Pie>
                </PieChart>
              </ResponsiveContainer>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  );
};

export default DashboardInsightsPage;
