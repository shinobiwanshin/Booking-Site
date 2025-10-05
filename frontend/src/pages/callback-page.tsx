// import { useEffect } from "react";
// import { useAuth } from "react-oidc-context";
// import { useNavigate } from "react-router";

// const CallbackPage: React.FC = () => {
//   const { isLoading, isAuthenticated } = useAuth();
//   const navigate = useNavigate();

//   useEffect(() => {
//     if (isLoading) {
//       return;
//     }

//     if (isAuthenticated) {
//       const redirectPath = localStorage.getItem("redirectPath");
//       if (redirectPath) {
//         localStorage.removeItem("redirectPath");
//         navigate(redirectPath);
//       }
//     }
//   }, [isLoading, isAuthenticated, navigate]);

//   if (isLoading) {
//     return <p>Processing login...</p>;
//   }

//   return <p>Completing login...</p>;
// };

// export default CallbackPage;
// filepath: /Users/amitabhanath/Documents/Booking-Capstone/frontend/src/pages/callback-page.tsx
import { useEffect } from "react";
import { useAuth } from "react-oidc-context";
import { useNavigate } from "react-router";

const CallbackPage: React.FC = () => {
  const { isLoading, isAuthenticated } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isLoading) {
      return;
    }

    if (isAuthenticated) {
      const redirectPath = localStorage.getItem("redirectPath") || "/"; // Default to main page
      localStorage.removeItem("redirectPath");
      navigate(redirectPath); // Redirect to the stored path or main page
    }
  }, [isLoading, isAuthenticated, navigate]);

  if (isLoading) {
    return <p>Processing login...</p>;
  }

  return <p>Completing login...</p>;
};

export default CallbackPage;
