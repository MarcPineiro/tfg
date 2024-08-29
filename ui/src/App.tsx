import React, { useEffect, useState } from 'react';
import FileManager from './components/FileManager';
import LoginSignUp from './components/LoginSignUp';
import { checkTokenAPI } from './utils/api';
import { ToastProvider, useToast } from './components/ToastProvider';

const App: React.FC = () => {
    const showToast = useToast();
    const [authenticated, setAuthenticated] = useState<boolean>(false);

    useEffect(() => {
        const validateToken = async () => {
            const isValid = await checkTokenAPI(useToast);
            setAuthenticated(isValid);
        };
        validateToken();
    }, []);

    return authenticated ? <ToastProvider><FileManager /></ToastProvider> : <ToastProvider><LoginSignUp onLoginSuccess={() => setAuthenticated(true)} /></ToastProvider>;
};

export default App;
