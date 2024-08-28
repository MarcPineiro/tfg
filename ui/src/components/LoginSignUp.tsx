import React, { useState } from 'react';
import { useToast } from './ToastProvider';
import '../css/LoginSignUp.css';
import { loginUserAPI, registerUserAPI } from '../utils/api';
import FileManager from './FileManager';
import Switch from "react-switch";

const LoginSignUp: React.FC<{ onLoginSuccess: () => void }>  = ({ onLoginSuccess }) => {
    const [isLogin, setIsLogin] = useState(true);
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [username, setUsername] = useState('test');
    const [password, setPassword] = useState('test');
    const [email, setEmail] = useState('');
    const [lastName, setLastName] = useState('');
    const [firstName, setFirstName] = useState('');
    const showToast = useToast();

    const toggleForm = () => {
        setIsLogin(!isLogin);
    };

    const handleLogin = async () => {
        try {
            await loginUserAPI({ username, password });
            setIsAuthenticated(true);
            showToast('Login successful!', 'info');
            onLoginSuccess();
        } catch (error) {
            showToast('Login failed. Please check your credentials.', 'error');
        }
    };

    const handleSignUp = async () => {
        try {
            await registerUserAPI({ username, password, email, firstName, lastName });
            setIsAuthenticated(true);
            showToast('Registration successful!', 'info');
            onLoginSuccess();
        } catch (error) {
            showToast('Registration failed. Please try again.', 'error');
        }
    };
    
    if (isAuthenticated) {
        return <FileManager />;
    }

    return (
        <div className="login-signup-container">
            <div className="slider">
                <label>{isLogin ? 'Login' : 'Sign Up'}</label>
                <Switch
                    onChange={toggleForm}
                    checked={!isLogin}
                    offColor="#888"
                    onColor="#0b76ef"
                    uncheckedIcon={false}
                    checkedIcon={false}
                />
            </div>

            <div className="form-container">
                {isLogin ? (
                    <div className="login-form">
                        <h2>Login</h2>
                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <button onClick={handleLogin}>Login</button>
                    </div>
                ) : (
                    <div className="signup-form">
                        <h2>Sign Up</h2>
                        <input
                            type="text"
                            placeholder="Username"
                            value={username}
                            onChange={(e) => setUsername(e.target.value)}
                        />
                        <input
                            type="email"
                            placeholder="E-mail"
                            value={email}
                            onChange={(e) => setEmail(e.target.value)}
                        />
                        <input
                            type="text"
                            placeholder="Fisrt Name"
                            value={username}
                            onChange={(e) => setFirstName(e.target.value)}
                        />
                        <input
                            type="text"
                            placeholder="Last Name"
                            value={username}
                            onChange={(e) => setLastName(e.target.value)}
                        />
                        <input
                            type="password"
                            placeholder="Password"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                        />
                        <button onClick={handleSignUp}>Sign Up</button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default LoginSignUp;
