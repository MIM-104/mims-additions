"""Fix literal .lang newline markers without changing any other quest content or layout."""
import copy
import json
import shutil
from datetime import datetime
from pathlib import Path

ROOT=Path(__file__).resolve().parents[1]
paths=[ROOT/'run/config/betterquesting/DefaultQuests.json']
paths+=list((ROOT/'run/config/betterquesting/saved_quests').glob('*.json'))
paths+=list((ROOT/'run/saves').glob('*/betterquesting/QuestDatabase.json'))
backup=ROOT/'build/quest-linebreak-backup'/datetime.now().strftime('%Y%m%d-%H%M%S')
total_fields=total_breaks=0
for path in paths:
    original=json.loads(path.read_text(encoding='utf-8'))
    data=copy.deepcopy(original);stats={'fields':0,'breaks':0}
    def fix(value):
        if isinstance(value,dict):
            for key,child in value.items():
                if key in ('name:8','desc:8') and isinstance(child,str) and '%n' in child:
                    stats['fields']+=1;stats['breaks']+=child.count('%n')
                    value[key]=child.replace('%n','\n')
                else:fix(child)
        elif isinstance(value,list):
            for child in value:fix(child)
    fix(data)
    if not stats['fields']:continue
    settings=data.get('questSettings:10',{}).get('betterquesting:10',{})
    if 'pack_version:3' in settings:settings['pack_version:3']+=1
    saved=backup/path.relative_to(ROOT);saved.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(path,saved)
    temp=path.with_suffix('.linebreaks.tmp')
    temp.write_text(json.dumps(data,indent=2,ensure_ascii=False)+'\n',encoding='utf-8')
    temp.replace(path)
    total_fields+=stats['fields'];total_breaks+=stats['breaks']
    print(f'{path.name}: fixed {stats["breaks"]} line breaks in {stats["fields"]} fields')
print(f'Total: {total_breaks} line breaks in {total_fields} fields. Backup: {backup}')
