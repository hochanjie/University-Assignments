import React from 'react';

interface InfoBarInterface {
    selectedXY: [number, number];
}

function InfoBar({ selectedXY }: InfoBarInterface) {
    return <div className="info-bar"> ({selectedXY[0]},{selectedXY[1]}) </div>
}

export default InfoBar;
