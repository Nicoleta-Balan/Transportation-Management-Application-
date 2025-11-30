import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import 'leaflet/dist/leaflet.css'
import './components/buttons/buttons.css'
import L from 'leaflet'
import { createDefaultMarkerIcon } from './utils/mapMarkers'

const DefaultIcon = createDefaultMarkerIcon();
L.Marker.prototype.options.icon = DefaultIcon;

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
