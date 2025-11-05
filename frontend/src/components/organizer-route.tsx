import { ReactNode } from "react";
import { Navigate, useLocation } from "react-router";
import { useRoles } from "@/hooks/use-roles";

interface OrganizerRouteProps {
  children: ReactNode;
}

const OrganizerRoute: React.FC<OrganizerRouteProps> = ({ children }) => {
  const { isLoading, isOrganizer } = useRoles();
  const location = useLocation();

  if (isLoading) {
    return <p>Loading...</p>;
  }

  if (!isOrganizer) {
    return <Navigate to="/dashboard" state={{ from: location }} replace />;
  }

  return children;
};

export default OrganizerRoute;
