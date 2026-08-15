import { CodeBlock } from '../../components/ui/CodeBlock'

export function DocArticle({ page }) {
  return (
    <div className="max-w-3xl">
      <p className="font-mono text-[11px] tracking-[0.22em] text-accent uppercase">{page.kicker}</p>
      <h1 className="display mt-3 text-4xl md:text-5xl">{page.title}</h1>
      <p className="mt-5 text-[17px] leading-8 text-ink-muted">{page.lead}</p>
      <div className="mt-14 space-y-14">
        {page.sections.map((section) => (
          <section key={section.title}>
            <h2 className="text-xl tracking-tight">{section.title}</h2>
            {section.paragraphs?.map((paragraph) => (
              <p key={paragraph} className="mt-3 text-sm leading-7 text-ink-muted">
                {paragraph}
              </p>
            ))}
            {section.bullets ? (
              <ul className="mt-4 space-y-2 text-sm leading-6 text-ink-muted">
                {section.bullets.map((item) => (
                  <li key={item} className="flex gap-3">
                    <span className="mt-[0.7em] h-px w-3 shrink-0 bg-accent" />
                    <span>{item}</span>
                  </li>
                ))}
              </ul>
            ) : null}
            {section.table ? (
              <div className="mt-4 overflow-x-auto border border-line">
                <table className="w-full min-w-[520px] text-left text-sm">
                  <thead className="bg-surface-2 text-[11px] tracking-[0.14em] text-ink-faint uppercase">
                    <tr>
                      {section.table.headers.map((header) => (
                        <th key={header} className="px-4 py-3 font-medium">
                          {header}
                        </th>
                      ))}
                    </tr>
                  </thead>
                  <tbody>
                    {section.table.rows.map((row) => (
                      <tr key={row.join()} className="border-t border-line">
                        {row.map((cell) => (
                          <td key={cell} className="px-4 py-3 align-top text-ink-muted">
                            {cell}
                          </td>
                        ))}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : null}
            {section.code ? <CodeBlock className="mt-4" language={section.code.language} code={section.code.code} /> : null}
          </section>
        ))}
      </div>
    </div>
  )
}
