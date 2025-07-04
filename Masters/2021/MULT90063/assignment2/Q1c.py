from qiskit import QuantumRegister, ClassicalRegister, QuantumCircuit
from numpy import pi

qreg_q = QuantumRegister(2, 'q')
creg_c = ClassicalRegister(2, 'c')
circuit = QuantumCircuit(qreg_q, creg_c)

circuit.ry(1*pi, qreg_q[0])
circuit.rx(0.1*pi, qreg_q[1])
circuit.cx(qreg_q[1], qreg_q[0])
circuit.h(qreg_q[1])
circuit.cx(qreg_q[1], qreg_q[0])
circuit.rz(0.4*pi, qreg_q[0])
circuit.rx(0.2*pi, qreg_q[1])
circuit.cx(qreg_q[1], qreg_q[0])
circuit.swap(qreg_q[0], qreg_q[1])
circuit.ry(0.5*pi, qreg_q[0])
circuit.rx(-0.6*pi, qreg_q[1])
circuit.cx(qreg_q[1], qreg_q[0])
circuit.h(qreg_q[1])
circuit.cx(qreg_q[1], qreg_q[0])
circuit.rz(0.4*pi, qreg_q[0])
circuit.rx(0.2*pi, qreg_q[1])
circuit.cx(qreg_q[1], qreg_q[0])
circuit.measure(qreg_q[0], creg_c[0])
circuit.measure(qreg_q[1], creg_c[1])