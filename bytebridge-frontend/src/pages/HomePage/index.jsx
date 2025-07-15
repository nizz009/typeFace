import React from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import './HomePage.css';

function HomePage() {
  const { isAuthenticated } = useAuth();

  return (
    <div className="homepage-container">
      <h1>Welcome to ByteBridge</h1>
      <p>Store, manage, and share your files securely.</p>
      {!isAuthenticated ?
        <div className="homepage-actions">
          <Link to="/signup" className="button primary">Get Started</Link>
          <Link to="/login" className="button secondary">Already have an account? Log In</Link>
        </div>
        :
        <div className="homepage-actions">
          <Link to="/profile" className="button primary">Get Started</Link>
        </div>
      }
    </div>
  );
}

export default HomePage;