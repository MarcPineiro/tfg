import React from 'react';
import '../css/FileDetails.css';

interface FileDetailsProps {
    file: { name: string; id: number; type: string };
}

const FileDetails: React.FC<FileDetailsProps> = ({ file }) => {
    return (
        <div className="file-details">
            <h3>File Details</h3>
            <p>Name: {file.name}</p>
            <p>Type: {file.type}</p>
            <p>ID: {file.id}</p>
        </div>
    );
};

export default FileDetails;
