import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import "./index.css";
import AttendeeLandingPage from "./pages/attendee-landing-page.tsx";
import { AuthProvider } from "react-oidc-context";
import { AuthContextProvider } from "./contexts/auth-context.tsx";
import { createBrowserRouter, RouterProvider } from "react-router";
import OrganizersLandingPage from "./pages/organizers-landing-page.tsx";
import DashboardManageEventPage from "./pages/dashboard-manage-event-page.tsx";
import LoginPage from "./pages/login-page.tsx";
import SignUpPage from "./pages/signup-page.tsx";
import WelcomeOnboardingPage from "./pages/welcome-onboarding-page.tsx";
import ProtectedRoute from "./components/protected-route.tsx";
import CallbackPage from "./pages/callback-page.tsx";
import DashboardListEventsPage from "./pages/dashboard-list-events-page.tsx";
import PublishedEventsPage from "./pages/published-events-page.tsx";
import PurchaseTicketPage from "./pages/purchase-ticket-page.tsx";
import DashboardListTickets from "./pages/dashboard-list-tickets.tsx";
import DashboardPage from "./pages/dashboard-page.tsx";
import DashboardViewTicketPage from "./pages/dashboard-view-ticket-page.tsx";
import DashboardValidateQrPage from "./pages/dashboard-validate-qr-page.tsx";
import DashboardInsightsPage from "./pages/dashboard-insights-page.tsx";
import OrganizerRoute from "./components/organizer-route.tsx";
import ResetPasswordPage from "./pages/reset-password-page.tsx";

const router = createBrowserRouter([
  {
    path: "/",
    Component: AttendeeLandingPage,
  },
  {
    path: "/callback",
    Component: CallbackPage,
  },
  {
    path: "/login",
    Component: LoginPage,
  },
  {
    path: "/signup",
    Component: SignUpPage,
  },
  {
    path: "/reset-password",
    Component: ResetPasswordPage,
  },
  {
    path: "/welcome",
    element: (
      <ProtectedRoute>
        <WelcomeOnboardingPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/events/:id",
    Component: PublishedEventsPage,
  },
  {
    path: "/events/:eventId/purchase/:ticketTypeId",
    element: (
      <ProtectedRoute>
        <PurchaseTicketPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/organizers",
    Component: OrganizersLandingPage,
  },
  {
    path: "/dashboard",
    element: (
      <ProtectedRoute>
        <DashboardPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/dashboard/insights",
    element: (
      <ProtectedRoute>
        <OrganizerRoute>
          <DashboardInsightsPage />
        </OrganizerRoute>
      </ProtectedRoute>
    ),
  },
  {
    path: "/dashboard/events",
    element: (
      <ProtectedRoute>
        <OrganizerRoute>
          <DashboardListEventsPage />
        </OrganizerRoute>
      </ProtectedRoute>
    ),
  },
  {
    path: "/dashboard/tickets",
    element: (
      <ProtectedRoute>
        <DashboardListTickets />
      </ProtectedRoute>
    ),
  },
  {
    path: "/dashboard/tickets/:id",
    element: (
      <ProtectedRoute>
        <DashboardViewTicketPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/dashboard/validate-qr",
    element: (
      <ProtectedRoute>
        <DashboardValidateQrPage />
      </ProtectedRoute>
    ),
  },
  {
    path: "/dashboard/events/create",
    element: (
      <ProtectedRoute>
        <OrganizerRoute>
          <DashboardManageEventPage />
        </OrganizerRoute>
      </ProtectedRoute>
    ),
  },
  {
    path: "/dashboard/events/update/:id",
    element: (
      <ProtectedRoute>
        <OrganizerRoute>
          <DashboardManageEventPage />
        </OrganizerRoute>
      </ProtectedRoute>
    ),
  },
]);

const oidcConfig = {
  authority: import.meta.env.VITE_KEYCLOAK_URL 
    ? `${import.meta.env.VITE_KEYCLOAK_URL}/realms/${import.meta.env.VITE_KEYCLOAK_REALM || 'event-ticket-platform'}`
    : "http://localhost:9090/realms/event-ticket-platform",
  client_id: import.meta.env.VITE_KEYCLOAK_CLIENT_ID || "event-ticket-platform-app",
  redirect_uri: window.location.origin + "/callback",
};

createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <AuthProvider {...oidcConfig}>
      <AuthContextProvider>
        <RouterProvider router={router} />
      </AuthContextProvider>
    </AuthProvider>
  </StrictMode>,
);
