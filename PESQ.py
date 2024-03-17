from scipy.io import wavfile
from pesq import pesq

rate, ref = wavfile.read("VoIPCourseWork\input.wav")
rate, deg = wavfile.read("VoIPCourseWork\output.wav")

print(pesq(rate, ref, deg, 'nb'))
