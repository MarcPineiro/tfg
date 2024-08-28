import React, { useEffect, useState } from 'react';
import FileManager from './components/FileManager';
import LoginSignUp from './components/LoginSignUp';
import { checkTokenAPI } from './utils/api';
import { ToastProvider } from './components/ToastProvider';

const App: React.FC = () => {
    const [authenticated, setAuthenticated] = useState<boolean>(false);

    useEffect(() => {
        const validateToken = async () => {
            const isValid = await checkTokenAPI();
            setAuthenticated(isValid);
        };
        validateToken();
    }, []);

    return authenticated ? <ToastProvider><FileManager /></ToastProvider> : <ToastProvider><LoginSignUp onLoginSuccess={() => setAuthenticated(true)} /></ToastProvider>;
};

export default App;
