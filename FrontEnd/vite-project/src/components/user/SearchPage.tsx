import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import './SearchPage.css';

export default function SearchPage() {
    const navigate = useNavigate();
    const [from, setFrom] = useState('');
    const [to, setTo] = useState('');
    const [date, setDate] = useState('');
    const [passengers, setPassengers] = useState(1);

    const handleSearch = (e: React.FormEvent) => {
        e.preventDefault();
        console.log('Searching for:', { from, to, date, passengers });
        // Implement search logic here
    };

    return (
        <div className="search-page">
            <header className="search-header">
                <div className="logo">BusBooking</div>
                <button className="admin-btn" onClick={() => navigate('/admin')}>
                    Admin Login
                </button>
            </header>
            
            <div className="hero-section">
                <div className="search-container">
                    <h1>Plan Your Journey</h1>
                    <form onSubmit={handleSearch} className="search-form">
                        <div className="form-group">
                            <label>From</label>
                            <input 
                                type="text" 
                                placeholder="City or Station" 
                                value={from}
                                onChange={(e) => setFrom(e.target.value)}
                            />
                        </div>
                        <div className="form-group">
                            <label>To</label>
                            <input 
                                type="text" 
                                placeholder="City or Station" 
                                value={to}
                                onChange={(e) => setTo(e.target.value)}
                            />
                        </div>
                        <div className="form-group">
                            <label>Departure</label>
                            <input 
                                type="date" 
                                value={date}
                                onChange={(e) => setDate(e.target.value)}
                            />
                        </div>
                        <div className="form-group">
                            <label>Passengers</label>
                            <input 
                                type="number" 
                                min="1" 
                                value={passengers}
                                onChange={(e) => setPassengers(parseInt(e.target.value))}
                            />
                        </div>
                        <button type="submit" className="search-btn">Search</button>
                    </form>
                </div>
            </div>
        </div>
    );
}
