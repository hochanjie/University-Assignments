from qiskit import QuantumRegister, ClassicalRegister, QuantumCircuit
from numpy import pi

qreg_q = QuantumRegister(3, 'q')
creg_c = ClassicalRegister(3, 'c')
circuit = QuantumCircuit(qreg_q, creg_c)

circuit.reset(qreg_q[0])
circuit.reset(qreg_q[1])
circuit.reset(qreg_q[2])
circuit.h(qreg_q[2])
circuit.cu(sqrt(3/8)*pi, 0, 0, 0, qreg_q[2], qreg_q[1])
circuit.cx(qreg_q[1], qreg_q[0])
circuit.h(qreg_q[0])
circuit.cx(qreg_q[2], qreg_q[1])
circuit.x(qreg_q[2])
circuit.crz(3*pi/2, qreg_q[1], qreg_q[2])
circuit.h(qreg_q[1])
circuit.h(qreg_q[2])
circuit.measure(qreg_q[0], creg_c[0])
circuit.measure(qreg_q[1], creg_c[1])
circuit.measure(qreg_q[2], creg_c[2])