import { useAuth } from "@/hooks/use-auth";
import { Avatar, AvatarFallback } from "./ui/avatar";
import { Button } from "./ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "./ui/dropdown-menu";
import { LogOut, LogIn } from "lucide-react";
import { useRoles } from "@/hooks/use-roles";
import { Link, useNavigate } from "react-router";

const NavBar: React.FC = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const { isOrganizer } = useRoles();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="bg-gray-950 border-b border-gray-800 text-white">
      <div className="container mx-auto p-4">
        <div className="flex justify-between items-center">
          <div className="flex gap-10 md:gap-20 items-center">
            <h1 className="text-xl font-bold">
              <Link to="/" className="text-xl font-bold hover:underline">
                Event Ticket Platform
              </Link>
            </h1>
            <div className="text-gray-300 flex gap-8">
              {isOrganizer && (
                <>
                  <Link to="/dashboard/events">Events</Link>
                  <Link to="/dashboard/insights">Dashboard</Link>
                </>
              )}
              <Link to="/dashboard/tickets">Tickets</Link>
            </div>
          </div>

          {isAuthenticated ? (
            <DropdownMenu>
              <DropdownMenuTrigger>
                <Avatar className="h-8 w-8">
                  <AvatarFallback className="bg-gray-700">
                    {user?.username?.slice(0, 2).toUpperCase()}
                  </AvatarFallback>
                </Avatar>
              </DropdownMenuTrigger>
              <DropdownMenuContent
                className="w-56 bg-gray-900 border-gray-700 text-white"
                align="end"
              >
                <DropdownMenuLabel className="font-normal">
                  <p className="text-sm font-medium">{user?.username}</p>
                  <p className="text-sm text-gray-400">{user?.role}</p>
                </DropdownMenuLabel>
                <DropdownMenuSeparator />
                <DropdownMenuItem
                  className="hover:bg-gray-800"
                  onClick={handleLogout}
                >
                  <LogOut />
                  <span>Log Out</span>
                </DropdownMenuItem>
              </DropdownMenuContent>
            </DropdownMenu>
          ) : (
            <Button
              onClick={() => navigate("/login")}
              className="bg-purple-600 hover:bg-purple-700"
            >
              <LogIn className="mr-2 h-4 w-4" />
              Log In
            </Button>
          )}
        </div>
      </div>
    </div>
  );
};

export default NavBar;
