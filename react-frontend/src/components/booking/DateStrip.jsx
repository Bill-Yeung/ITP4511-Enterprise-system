export function DateStrip({
  stripDates,
  dateOffset,
  selectedDate,
  visibleDays,
  isDayClosed,
  isPastDate,
  isToday,
  toDateStr,
  onDateSelect,
  onDateOffsetChange,
  onResetDate,
}) {

  const shortDayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];
  const shortMonthNames = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];

  return (
    <div className="date-strip-container">
      <button
        className="date-strip-arrow"
        onClick={() => onDateOffsetChange(Math.max(0, dateOffset - visibleDays))}
        disabled={dateOffset <= 0}
      >
        &#9664;
      </button>

      <div className="date-strip">
        {stripDates.map(d => {
          const dateStr = toDateStr(d);
          const closed = isDayClosed(d);
          const past = isPastDate(d);
          const today = isToday(d);
          const active = selectedDate && toDateStr(selectedDate) === dateStr;
          const disabled = closed || past;

          return (
            <button
              key={dateStr}
              className={`date-strip-item ${active ? 'active' : ''} ${today ? 'today' : ''} ${disabled ? 'disabled' : ''} ${closed && !past ? 'closed' : ''}`}
              onClick={() => !disabled && onDateSelect(d)}
              disabled={disabled}
            >
              <span className="date-strip-day">{shortDayNames[d.getDay()]}</span>
              <span className="date-strip-num">{d.getDate()}</span>
              <span className="date-strip-month">{shortMonthNames[d.getMonth()]}</span>
            </button>
          );
        })}
      </div>

      <button
        className="date-strip-arrow"
        onClick={() => onDateOffsetChange(dateOffset + visibleDays)}
      >
        &#9654;
      </button>

      {dateOffset > 0 && (
        <button className="date-strip-today-btn" onClick={onResetDate}>
          Today
        </button>
      )}
    </div>
  );
  
}
