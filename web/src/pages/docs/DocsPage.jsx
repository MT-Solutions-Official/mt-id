import { useParams } from 'react-router-dom'
import { docs } from '../../content/docs'
import { DocArticle } from './DocArticle'

const slugMap = {
  undefined: 'overview',
  '': 'overview',
  overview: 'overview',
  quickstart: 'quickstart',
  architecture: 'architecture',
  'application-auth': 'application-auth',
  users: 'users',
  'user-auth': 'user-auth',
  google: 'google',
  cors: 'cors',
  emails: 'emails',
  errors: 'errors',
  addresses: 'addresses',
}

export function DocsPage() {
  const { slug } = useParams()
  const page = docs[slugMap[slug] || 'overview']
  if (!page) {
    return <p className="text-ink-muted">Página não encontrada.</p>
  }
  return <DocArticle page={page} />
}
