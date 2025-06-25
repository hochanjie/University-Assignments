import React from 'react';

interface CellProps {
    rowI: number;
    colI: number;
    color: string;
    setSelectedXY: (selectedXY: [number, number]) => void;
    selectedXY: [number, number]
}


function Cell({ rowI, colI, color, setSelectedXY, selectedXY }: CellProps) {
    const focused = selectedXY[0] == rowI && selectedXY[1] == colI;
    const style = {
        backgroundColor:  color,
    }
    return (
        <div className="cell" style={style} 
            onClick={() => setSelectedXY([rowI, colI])} >
            { focused ? <img className="selector" src={process.env.PUBLIC_URL + "selector-3.png"} /> : null}
        </div>
    );
}

export default Cell;
