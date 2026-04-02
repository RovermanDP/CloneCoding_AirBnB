export function Topbar() {
  return (
    <div className="topbar">
      <input
        className="search-box"
        type="search"
        placeholder="자산, 티켓, 임대인, 세입자 검색"
        aria-label="Search tickets and properties"
      />
      <button className="ghost-button" type="button">
        Seongsu Portfolio
      </button>
      <button className="primary-button" type="button">
        새 작업 등록
      </button>
    </div>
  );
}

