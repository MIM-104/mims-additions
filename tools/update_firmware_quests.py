"""Update saved quest text without resetting the player-edited questbook."""
from pathlib import Path
import runpy
if __name__ == '__main__':
    runpy.run_path(str(Path(__file__).with_name('make_quests_editable.py')), run_name='__main__')
