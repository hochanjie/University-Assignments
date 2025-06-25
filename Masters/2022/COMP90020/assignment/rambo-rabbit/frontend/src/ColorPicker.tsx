import React, { useState } from 'react';

const PALETTE: string[] = ["#ff4500", "#ffa800", "#ffd635", "#00a268", "#7eed56", "#2450a4", "#3690ea", "#51e9f4", "#811e9f", "#b44ac0", "#ff99aa", "#9c6926", "#000000", "#898d90", "#d4d7d9", "#ffffff"]
const CELL_SIZE = 30;
const NO_SELECTION = ""

interface ColorPickerProps {
    updateCellColor: (color: string) => void;
}

function ColorPicker({ updateCellColor }: ColorPickerProps): JSX.Element {
    const [selectedColor, setSelectedColor] = useState(NO_SELECTION);

    function ColorCell({ color }: { color: string }): JSX.Element {
        const style = {
            backgroundColor: color,
            width: `${selectedColor == color ? CELL_SIZE * 1.5 : CELL_SIZE}px`,
            height: `${selectedColor == color ? CELL_SIZE * 2 : CELL_SIZE}px`,
        }
        return <div className="color-cell" style={style} id={color}
            onClick={() => updateCellColor(color)} >
        </div>
    }

    function onMove(event: React.MouseEvent<HTMLElement>): void {
        const target = (event.target as HTMLElement)
        if (PALETTE.includes(target.id) && target.id != selectedColor) {
            setSelectedColor(target.id)
        }
    }

    function onMouseLeave(): void {
        setSelectedColor(NO_SELECTION)
    }

    // function onTouchMove(event: React.TouchEvent): void {
    //     const x = event.targetTouches[0].clientX
    //     const y = event.targetTouches[0].clientY
    //     const target = document.elementFromPoint(x, y)
    //     console.log(x, y, target)
    //     if (target) {
    //         console.log(target.id)
    //         if (PALETTE.includes(target.id) && target.id != selectedColor) {
    //             setSelectedColor(target.id)
    //         }
    //     }
    // }

    const style = { width: `${10.5 * CELL_SIZE}px` }
    return <div className="color-picker" style={style} onMouseMove={onMove} onMouseLeave={onMouseLeave}> {
            PALETTE.map(color => <ColorCell color={color} key={color} />)
        } </div>
}

export default ColorPicker;
