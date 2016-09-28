import time, sys
sys.path.append("simplejson.whl")
import simplejson as json
from com.android.monkeyrunner import MonkeyRunner, MonkeyDevice

def play(device):
	pos = [240, 500]
	c = open('309_4', 'r+').read()
	raw_notes = json.loads(c)
	notes = []
	
	for raw_note in raw_notes:
		if raw_note['type'] == '1' or raw_note['type'] == 1:
			notes.append({'id' : raw_note['id'],'pos' : int(raw_note['finishPos']), 'sec' : float(raw_note['sec'])})
	
	# input("Press enter when music starts...")
	print("sleeping", notes[0]['sec'])
	time.sleep(notes[0]['sec'])
	device.touch(pos[0], pos[1] * notes[0]['pos'], MonkeyDevice.DOWN_AND_UP)
	time.sleep(notes[1]['sec'] - notes[0]['sec'])
	for i in range(1, len(notes)):

		delay = notes[i + 1]['sec'] - notes[i]['sec']
		print("sleeping", delay)
		time.sleep(delay)
		device.touch(pos[0], pos[1] * notes[i]['pos'], MonkeyDevice.DOWN_AND_UP)

device = MonkeyRunner.waitForConnection()
play(device)