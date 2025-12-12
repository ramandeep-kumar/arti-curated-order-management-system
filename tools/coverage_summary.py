import csv
import glob
import xml.etree.ElementTree as ET
from pathlib import Path

repo_root = Path(__file__).resolve().parents[1]
jacoco_csv = repo_root / 'target' / 'site' / 'jacoco' / 'jacoco.csv'
surefire_dir = repo_root / 'target' / 'surefire-reports'
output_md = repo_root / 'coverage' / 'unit_test_coverage_report.md'

summary = {}

if jacoco_csv.exists():
    with jacoco_csv.open() as f:
        reader = csv.DictReader(f)
        totals = {
            'INSTRUCTION_MISSED': 0,
            'INSTRUCTION_COVERED': 0,
            'BRANCH_MISSED': 0,
            'BRANCH_COVERED': 0,
            'LINE_MISSED': 0,
            'LINE_COVERED': 0,
            'COMPLEXITY_MISSED': 0,
            'COMPLEXITY_COVERED': 0,
            'METHOD_MISSED': 0,
            'METHOD_COVERED': 0,
        }
        classes = []
        for row in reader:
            # skip header rows that might not contain numeric values
            try:
                im = int(row.get('INSTRUCTION_MISSED', 0))
            except:
                continue
            for k in totals.keys():
                totals[k] += int(row.get(k, 0) or 0)
            classes.append((row.get('CLASS') or row.get('PACKAGE') or '', int(row.get('INSTRUCTION_MISSED',0)), int(row.get('INSTRUCTION_COVERED',0))))

        summary['jacoco_totals'] = totals
        # compute percentages
        def pct(cov, miss):
            total = cov + miss
            return (cov / total * 100) if total>0 else 0.0
        summary['instruction_pct'] = pct(totals['INSTRUCTION_COVERED'], totals['INSTRUCTION_MISSED'])
        summary['branch_pct'] = pct(totals['BRANCH_COVERED'], totals['BRANCH_MISSED'])
        summary['line_pct'] = pct(totals['LINE_COVERED'], totals['LINE_MISSED'])
        summary['method_pct'] = pct(totals['METHOD_COVERED'], totals['METHOD_MISSED'])
        # top offenders by missed instructions
        classes_sorted = sorted(classes, key=lambda x: x[1], reverse=True)
        summary['top_missed'] = classes_sorted[:10]
else:
    summary['jacoco_totals'] = None

# aggregate surefire XMLs
import math

tests = failures = errors = skipped = 0
time_total = 0.0
xml_files = list(surefire_dir.glob('TEST-*.xml')) if surefire_dir.exists() else []
for xf in xml_files:
    try:
        tree = ET.parse(xf)
        root = tree.getroot()
        tests += int(root.attrib.get('tests',0))
        failures += int(root.attrib.get('failures',0))
        errors += int(root.attrib.get('errors',0))
        skipped += int(root.attrib.get('skipped',0))
        time_total += float(root.attrib.get('time',0.0))
    except Exception as e:
        # ignore parse errors
        pass

summary['tests_run'] = tests
summary['failures'] = failures
summary['errors'] = errors
summary['skipped'] = skipped
summary['time'] = time_total
summary['xml_files_count'] = len(xml_files)

# write markdown
md_lines = []
md_lines.append('# Unit & Integration Test Coverage Report')
md_lines.append('')
md_lines.append(f'Generated: {Path().cwd().name}')
md_lines.append('')
md_lines.append('## Test Suite Summary')
md_lines.append('')
md_lines.append(f'- Surefire XML files processed: {summary["xml_files_count"]}')
md_lines.append(f'- Tests run: **{summary["tests_run"]}**')
md_lines.append(f'- Failures: **{summary["failures"]}**')
md_lines.append(f'- Errors: **{summary["errors"]}**')
md_lines.append(f'- Skipped: **{summary["skipped"]}**')
md_lines.append(f'- Total test time (s): {summary["time"]:.2f}')
md_lines.append('')

if summary['jacoco_totals'] is None:
    md_lines.append('No JaCoCo CSV report found at `target/site/jacoco/jacoco.csv`. Please run `mvn test` with JaCoCo enabled to generate coverage data.')
else:
    t = summary['jacoco_totals']
    md_lines.append('## JaCoCo Aggregate Coverage')
    md_lines.append('')
    md_lines.append('| Metric | Covered | Missed | Total | Coverage % |')
    md_lines.append('|---|---:|---:|---:|---:|')
    def row(name, covered, missed):
        total = covered + missed
        pct = (covered/total*100) if total>0 else 0.0
        return f'| {name} | {covered} | {missed} | {total} | {pct:.2f}% |'
    md_lines.append(row('Instructions', t['INSTRUCTION_COVERED'], t['INSTRUCTION_MISSED']))
    md_lines.append(row('Branches', t['BRANCH_COVERED'], t['BRANCH_MISSED']))
    md_lines.append(row('Lines', t['LINE_COVERED'], t['LINE_MISSED']))
    md_lines.append(row('Methods', t['METHOD_COVERED'], t['METHOD_MISSED']))
    md_lines.append('')
    md_lines.append(f'- Instruction coverage: **{summary["instruction_pct"]:.2f}%**')
    md_lines.append(f'- Branch coverage: **{summary["branch_pct"]:.2f}%**')
    md_lines.append(f'- Line coverage: **{summary["line_pct"]:.2f}%**')
    md_lines.append(f'- Method coverage: **{summary["method_pct"]:.2f}%**')
    md_lines.append('')
    md_lines.append('### Top classes by missed instructions')
    md_lines.append('')
    md_lines.append('| Class | Instruction missed | Instruction covered |')
    md_lines.append('|---|---:|---:|')
    for cls, missed, covered in summary['top_missed']:
        md_lines.append(f'| {cls} | {missed} | {covered} |')

md_lines.append('')
md_lines.append('## Notes')
md_lines.append('')
md_lines.append('- If integration tests using Testcontainers failed due to Docker not available locally, run the CI workflow on GitHub Actions or run locally with Docker running.')
md_lines.append('- Full HTML report is available at `target/site/jacoco/index.html` when generated.')

output_md.parent.mkdir(parents=True, exist_ok=True)
output_md.write_text('\n'.join(md_lines), encoding='utf-8')
print('WROTE', output_md)
