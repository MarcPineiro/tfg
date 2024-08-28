import React from 'react';
import '../css/Header.css';

const Header: React.FC<{handleUserSettings: () => void}> = ({handleUserSettings}) => {
    return (
        <header className="header">
            <div className="user-settings">
                <button onClick={handleUserSettings}>User Settings</button>
            </div>
        </header>
    );
};

export default Header;
