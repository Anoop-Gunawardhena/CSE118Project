from apa102.py import APA102
import time
    
    
strip = apa102.APA102(num_led=12, order='rgb')
for i in range(12):
    strip.set_pixel_rgb(i,0x00FF00)

strip.show()
time.sleep(1000)
strip.cleanup()

