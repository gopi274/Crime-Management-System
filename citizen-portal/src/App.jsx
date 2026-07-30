import React, { useState, useEffect } from 'react';
import { HashRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { motion, AnimatePresence } from 'framer-motion';
import { Shield, FileWarning, Search, Activity, ChevronRight, Lock } from 'lucide-react';
import axios from 'axios';

// API Configuration
const API_BASE = '/api';

/**
 * Main Layout wrapping the routes
 */
/**
 * Login Component
 */
const Login = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post(`${API_BASE}/auth/login`, { username, password });
      localStorage.setItem('token', res.data.data.token);
      localStorage.setItem('user', JSON.stringify(res.data.data));
      window.location.href = '/';
    } catch (err) {
      setError('Invalid credentials or server unreachable.');
    }
  };

  return (
    <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} style={{ maxWidth: '400px', margin: '80px auto' }}>
      <div className="glass-panel" style={{ padding: '40px' }}>
        <h2 style={{ fontSize: '2rem', marginBottom: '10px', textAlign: 'center' }}>Portal Login</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '30px', textAlign: 'center' }}>Secure access for verified citizens</p>
        
        {error && <div style={{ color: 'var(--danger)', marginBottom: '20px', textAlign: 'center' }}>{error}</div>}
        
        <form onSubmit={handleLogin} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '8px' }}>Username</label>
            <input required type="text" className="input-glass" value={username} onChange={e => setUsername(e.target.value)} />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '8px' }}>Password</label>
            <input required type="password" className="input-glass" value={password} onChange={e => setPassword(e.target.value)} />
          </div>
          <button type="submit" className="btn btn-primary" style={{ padding: '14px' }}>
            <Lock size={18} /> Authenticate
          </button>
        </form>
      </div>
    </motion.div>
  );
};

const Layout = ({ children }) => {
  const user = JSON.parse(localStorage.getItem('user'));
  
  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/';
  };

  return (
    <div className="app-wrapper">
      <div className="bg-mesh"></div>
      
      <nav className="glass-panel" style={{ margin: '20px', padding: '15px 30px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <Link to="/" style={{ textDecoration: 'none', color: 'white', display: 'flex', alignItems: 'center', gap: '10px' }}>
          <Shield size={28} color="#3b82f6" />
          <span style={{ fontSize: '1.2rem', fontWeight: 700, letterSpacing: '1px' }}>KERALA POLICE ONLINE</span>
        </Link>
        
        <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
          <Link to="/report" className="btn btn-primary" style={{ textDecoration: 'none' }}>
            <FileWarning size={18} /> File e-Complaint
          </Link>
          <Link to="/track" className="btn btn-glass" style={{ textDecoration: 'none' }}>
            <Search size={18} /> Track Status
          </Link>
          {user ? (
             <button onClick={handleLogout} className="btn btn-glass" style={{ border: '1px solid var(--danger)', color: 'var(--danger)' }}>Logout ({user.username})</button>
          ) : (
            <Link to="/login" className="btn btn-glass" style={{ textDecoration: 'none' }}><Lock size={16} /> Login</Link>
          )}
        </div>
      </nav>

      <main className="container" style={{ paddingBottom: '60px' }}>
        <AnimatePresence mode="wait">
          {children}
        </AnimatePresence>
      </main>
    </div>
  );
};

/**
 * Public Dashboard (Home)
 */
const Dashboard = () => {
  const [stats, setStats] = useState({ openCases: 0, closedCases: 0, activeOfficers: 0 });
  const [verdicts, setVerdicts] = useState([]);
  const [selectedCase, setSelectedCase] = useState(null);

  useEffect(() => {
    axios.get(`${API_BASE}/public/stats`).then(res => setStats(res.data)).catch(console.error);
    axios.get(`${API_BASE}/public/stories`).then(res => setVerdicts(res.data)).catch(console.error);
  }, []);

  const openDetails = async (caseId) => {
    try {
      const res = await axios.get(`${API_BASE}/public/case-details/${caseId}`);
      setSelectedCase(res.data);
    } catch (err) {
      alert("Unauthorized: Forensic details only available for closed/public cases.");
    }
  };

  return (
    <motion.div initial={{ opacity: 0, y: 20 }} animate={{ opacity: 1, y: 0 }} exit={{ opacity: 0, y: -20 }}>
      {/* Hero Section */}
      <section style={{ textAlign: 'center', padding: '80px 0 60px 0' }}>
        <motion.h1 
          className="title-gradient"
          style={{ fontSize: '3.5rem', marginBottom: '20px' }}
          initial={{ scale: 0.9 }} animate={{ scale: 1 }}
        >
          Secure. Transparent. Anonymous.
        </motion.h1>
        <p style={{ fontSize: '1.2rem', color: 'var(--text-muted)', maxWidth: '600px', margin: '0 auto' }}>
          The official citizen portal for recording complaints directly to the police and tracking your case files 24/7.
        </p>
      </section>

      {/* Live Stats */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(250px, 1fr))', gap: '24px', marginBottom: '60px' }}>
        <StatCard title="Active Police Officers" value={stats.activeOfficers} icon={<Shield color="var(--primary)" />} />
        <StatCard title="Cases Solved (YTD)" value={stats.closedCases} icon={<Activity color="var(--accent)" />} />
        <StatCard title="Open Investigations" value={stats.openCases} icon={<Search color="var(--secondary)" />} />
      </div>

      {/* Public Verdicts */}
      <h2 style={{ fontSize: '2rem', marginBottom: '30px', display: 'flex', alignItems: 'center', gap: '12px' }}>
        <Activity color="var(--accent)" /> Verified Judicial Verdicts (BNS)
      </h2>
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(350px, 1fr))', gap: '24px' }}>
        {verdicts.length > 0 ? verdicts.map((story, i) => (
          <motion.div key={i} className="glass-card" style={{ padding: '30px' }} whileHover={{ scale: 1.02 }}>
            <span style={{ fontSize: '0.85rem', color: 'var(--accent)', fontWeight: 600 }}>OFFICIAL VERDICT • {story.date}</span>
            <h3 style={{ fontSize: '1.5rem', margin: '10px 0', color: 'white' }}>{story.title}</h3>
            <p style={{ color: 'var(--text-muted)', lineHeight: 1.6, maxHeight: '100px', overflow: 'hidden' }}>{story.summary}</p>
            <button 
                onClick={() => openDetails(story.id)}
                className="btn btn-glass" 
                style={{ marginTop: '20px', width: '100%', justifyContent: 'center' }}
            >
              View Court Records <ChevronRight size={16} />
            </button>
          </motion.div>
        )) : (
          <div style={{ color: 'var(--text-muted)', padding: '20px', background: 'rgba(255,255,255,0.05)', borderRadius: '12px' }}>
            No public verdicts published yet. Only Admin-verified case resolutions are listed here for public transparency.
          </div>
        )}
      </div>

      {/* Case Details Modal */}
      {selectedCase && (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, background: 'rgba(0,0,0,0.85)', zIndex: 1000, display: 'flex', alignItems: 'center', justifyContent: 'center', padding: '20px' }}>
          <motion.div initial={{ scale: 0.9, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} className="glass-panel" style={{ maxWidth: '800px', width: '100%', maxHeight: '90vh', overflowY: 'auto', padding: '40px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                <span className="stat-value" style={{ fontSize: '1.2rem', color: 'var(--primary)' }}>Case ID: {selectedCase.caseNumber}</span>
                <button onClick={() => setSelectedCase(null)} style={{ border: 'none', background: 'none', color: 'white', cursor: 'pointer', fontSize: '1.5rem' }}>&times;</button>
            </div>
            <h2 style={{ fontSize: '2rem', marginBottom: '20px' }}>{selectedCase.title}</h2>
            <div style={{ background: 'rgba(0,180,255,0.1)', border: '1px solid var(--primary)', padding: '15px', borderRadius: '10px', marginBottom: '20px' }}>
                <span style={{ fontWeight: 800, color: 'var(--accent)' }}>Legal Status: </span>
                <span>{selectedCase.status} - Judicially Verified</span>
            </div>
               <p style={{ lineHeight: 1.8, marginBottom: '20px', fontSize: '1.1rem' }}>{selectedCase.description}</p>
            
            {selectedCase.article && (
              <div style={{ background: 'rgba(255,215,0,0.05)', borderLeft: '4px solid var(--accent)', padding: '25px', borderRadius: '8px', marginBottom: '30px' }}>
                <h4 style={{ color: 'var(--accent)', marginBottom: '15px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Shield size={18} /> Official Judicial Article
                </h4>
                <div style={{ lineHeight: 2.0, fontSize: '1.15rem', color: 'var(--text-primary)', whiteSpace: 'pre-wrap' }}>
                  {selectedCase.article}
                </div>
              </div>
            )}

            <div style={{ background: 'rgba(255,255,255,0.05)', padding: '20px', borderRadius: '12px' }}>
                <h4 style={{ color: 'var(--secondary)', marginBottom: '10px' }}>Bharatiya Nyaya Sanhita (BNS) Charges</h4>
                <p style={{ color: 'var(--text-muted)' }}>{selectedCase.charges}</p>
                {selectedCase.verdict && (
                  <p style={{ marginTop: '10px', color: 'var(--primary)', fontWeight: 600 }}>Verdict: {selectedCase.verdict}</p>
                )}
            </div>
            
            <button onClick={() => setSelectedCase(null)} className="btn btn-primary" style={{ marginTop: '30px', width: '100%', padding: '15px' }}>Close Case File</button>
          </motion.div>
        </div>
      )}
    </motion.div>
  );
};

const StatCard = ({ title, value, icon }) => (
  <div className="glass-card" style={{ padding: '30px', display: 'flex', flexDirection: 'column', gap: '15px' }}>
    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
      <h4 style={{ color: 'var(--text-muted)', fontSize: '1rem', fontWeight: 500 }}>{title}</h4>
      {icon}
    </div>
    <div className="stat-value title-gradient">{value.toLocaleString()}</div>
  </div>
);

/**
 * e-Complaint AI Form
 */
const ComplaintForm = () => {
  const [desc, setDesc] = useState('');
  const [loc, setLoc] = useState('');
  const [status, setStatus] = useState(null);

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      const res = await axios.post(`${API_BASE}/public/complaints`, { description: desc, location: loc });
      setStatus(res.data);
    } catch (err) {
      alert("Error securely syncing with the network.");
    }
  };

  if (status) return (
    <motion.div initial={{ opacity: 0 }} animate={{ opacity: 1 }} className="glass-panel" style={{ padding: '60px', textAlign: 'center', maxWidth: '600px', margin: '80px auto' }}>
      <Shield size={64} color="var(--accent)" style={{ margin: '0 auto 24px auto' }} />
      <h2 style={{ fontSize: '2rem', marginBottom: '16px' }}>{status.message}</h2>
      <p style={{ color: 'var(--text-muted)', marginBottom: '30px' }}>Your complaint has been successfully received and forwarded directly to your local police station.</p>
      
      <div style={{ background: 'rgba(0,0,0,0.3)', padding: '20px', borderRadius: '12px', marginBottom: '30px' }}>
        <p style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '8px' }}>Your Secure Tracking ID</p>
        <p style={{ fontSize: '1.5rem', fontFamily: 'monospace', color: 'var(--primary)', letterSpacing: '2px' }}>{status.trackingId}</p>
      </div>
    </motion.div>
  );

  return (
    <motion.div initial={{ opacity: 0, x: -20 }} animate={{ opacity: 1, x: 0 }} exit={{ opacity: 0, x: 20 }} style={{ maxWidth: '700px', margin: '40px auto' }}>
      <div className="glass-panel" style={{ padding: '40px' }}>
        <h2 style={{ fontSize: '2.2rem', marginBottom: '10px' }}>File an e-Complaint</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '40px' }}>Your report will be sent directly to the police station for immediate action. No need to visit the station in person.</p>
        
        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '24px' }}>
          <div>
            <label style={{ display: 'block', marginBottom: '10px', fontWeight: 600 }}>Emergency Location (City/Pincode)</label>
            <input required type="text" className="input-glass" value={loc} onChange={e => setLoc(e.target.value)} placeholder="e.g. Trivandrum Central" />
          </div>
          <div>
            <label style={{ display: 'block', marginBottom: '10px', fontWeight: 600 }}>Detailed Description</label>
            <textarea required className="input-glass" value={desc} onChange={e => setDesc(e.target.value)} rows="5" placeholder="Explain what happened..."></textarea>
          </div>
          
          <button type="submit" className="btn btn-primary" style={{ padding: '16px', fontSize: '1.1rem', marginTop: '10px' }}>
            <FileWarning size={20} /> Submit Securely to Dispatch
          </button>
        </form>
      </div>
    </motion.div>
  );
};

/**
 * Track Status Form
 */
const TrackStatus = () => {
  const [trackId, setTrackId] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');

  const handleSearch = async (e) => {
    e.preventDefault();
    if (!trackId) return;
    setError('');
    setResult(null);
    try {
      const res = await axios.get(`${API_BASE}/public/track/${trackId}`);
      setResult(res.data);
    } catch (err) {
      if (err.response && err.response.status === 404) {
        setError('No records found. Please check your tracking ID / FIR No.');
      } else {
        setError('An error occurred connecting to the backend.');
      }
    }
  };

  return (
    <motion.div initial={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }} exit={{ opacity: 0 }} style={{ maxWidth: '700px', margin: '40px auto' }}>
      <div className="glass-panel" style={{ padding: '40px' }}>
        <h2 style={{ fontSize: '2.2rem', marginBottom: '10px' }}>Track Case Status</h2>
        <p style={{ color: 'var(--text-muted)', marginBottom: '30px' }}>Enter your e-Complaint Tracking ID, FIR Number, or Case Number to fetch real-time updates.</p>
        
        <form onSubmit={handleSearch} style={{ display: 'flex', gap: '15px', marginBottom: '40px' }}>
          <input required type="text" className="input-glass" value={trackId} onChange={e => setTrackId(e.target.value)} placeholder="e.g. TRACK-123456 or FIR-2026-001" style={{ flex: 1 }} />
          <button type="submit" className="btn btn-primary" style={{ padding: '16px 24px' }}>
            <Search size={20} />
          </button>
        </form>

        {error && <div style={{ color: 'var(--danger)', background: 'rgba(255, 50, 80, 0.1)', padding: '15px', borderRadius: '8px', border: '1px solid rgba(255, 50, 80, 0.3)' }}>{error}</div>}

        <AnimatePresence>
          {result && (
            <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="glass-card" style={{ padding: '30px', marginTop: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
                <span style={{ padding: '5px 12px', background: 'rgba(59, 130, 246, 0.2)', color: 'var(--primary)', borderRadius: '20px', fontSize: '0.85rem', fontWeight: 'bold' }}>
                  {result.type}
                </span>
                <span style={{ fontFamily: 'monospace', fontSize: '1.2rem', fontWeight: 'bold' }}>{result.id}</span>
              </div>
              
              <div style={{ padding: '20px', background: 'rgba(0,0,0,0.2)', borderRadius: '12px', marginBottom: '20px' }}>
                <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '5px' }}>Current Status</div>
                <div style={{ fontSize: '1.8rem', color: 'var(--accent)', fontWeight: 800 }}>{result.status}</div>
              </div>

              {result.description && (
                <div style={{ marginBottom: '20px' }}>
                  <div style={{ fontSize: '0.9rem', color: 'var(--text-muted)', marginBottom: '5px' }}>Description</div>
                  <div style={{ lineHeight: 1.6 }}>{result.description}</div>
                </div>
              )}

              {result.caseNumber && (
                <div style={{ display: 'flex', justifyContent: 'space-between', padding: '15px', border: '1px solid var(--glass-border)', borderRadius: '12px' }}>
                  <div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Linked Case File</div>
                    <div style={{ fontWeight: 'bold', color: 'var(--primary)' }}>{result.caseNumber} ({result.caseStatus})</div>
                  </div>
                </div>
              )}

              {result.nextTrialDate && (
                <div style={{ marginTop: '15px', padding: '15px', background: 'rgba(139, 92, 246, 0.15)', borderRadius: '12px', display: 'flex', alignItems: 'center', gap: '10px' }}>
                  <Activity color="var(--secondary)" />
                  <div>
                    <div style={{ fontSize: '0.85rem', color: 'var(--text-muted)' }}>Scheduled Court Appearance</div>
                    <div style={{ fontWeight: 'bold' }}>{result.nextTrialDate}</div>
                  </div>
                </div>
              )}
            </motion.div>
          )}
        </AnimatePresence>
      </div>
    </motion.div>
  );
};

export default function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<Dashboard />} />
          <Route path="/report" element={<ComplaintForm />} />
          <Route path="/track" element={<TrackStatus />} />
          <Route path="/login" element={<Login />} />
        </Routes>
      </Layout>
    </Router>
  );
}
