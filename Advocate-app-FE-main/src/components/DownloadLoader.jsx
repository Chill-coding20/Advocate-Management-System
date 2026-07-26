import "../assets/styles/DownloadLoader.css";

export function DownloadLoader({ message, size = 50 }) {
  return (
    <div className="dl-overlay">
      <div className="dl-container">
        <div className="dl-loader" style={{ width: size, height: size }} />
        {message && <p className="dl-message">{message}</p>}
      </div>
    </div>
  );
}

export default DownloadLoader;
